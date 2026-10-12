package fr.luteal.app.calendar

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.calendar.CalendarImportEngine
import fr.luteal.core.calendar.ImportedCalendarInstance
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarInstancesReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readInstances(
        calendarIds: Set<Long>,
        fromInclusive: Instant,
        toExclusive: Instant
    ): List<ImportedCalendarInstance> {
        if (calendarIds.isEmpty()) return emptyList()
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, fromInclusive.toEpochMilli())
        ContentUris.appendId(builder, toExclusive.toEpochMilli())
        val uri = builder.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_ID
        )
        val selection = "${CalendarContract.Instances.CALENDAR_ID} IN (${calendarIds.joinToString(",")})"
        val instances = mutableListOf<ImportedCalendarInstance>()
        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                instances += cursor.toInstance()
            }
        }
        return instances
    }

    private fun Cursor.toInstance(): ImportedCalendarInstance {
        val eventId = getLong(getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID))
        val begin = getLong(getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
        val end = getLong(getColumnIndexOrThrow(CalendarContract.Instances.END))
        val title = getString(getColumnIndexOrThrow(CalendarContract.Instances.TITLE)) ?: ""
        val description = getString(getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)) ?: ""
        val location = getString(getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)) ?: ""
        return ImportedCalendarInstance(
            calendarEventKey = CalendarImportEngine.calendarEventKey(eventId, begin),
            title = title,
            description = description,
            location = location,
            startAtEpochMillis = begin,
            endAtEpochMillis = end.takeIf { it > 0 }
        )
    }
}

data class DeviceCalendar(
    val id: Long,
    val displayName: String,
    val accountName: String
)

@Singleton
class DeviceCalendarCatalog @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun listCalendars(): List<DeviceCalendar> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val calendars = mutableListOf<DeviceCalendar>()
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            while (cursor.moveToNext()) {
                calendars += DeviceCalendar(
                    id = cursor.getLong(idIndex),
                    displayName = cursor.getString(nameIndex) ?: "",
                    accountName = cursor.getString(accountIndex) ?: ""
                )
            }
        }
        return calendars.sortedBy { it.displayName.lowercase() }
    }
}
