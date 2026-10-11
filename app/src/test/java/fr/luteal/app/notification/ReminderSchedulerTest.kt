package fr.luteal.app.notification

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.model.ReminderStatus
import fr.luteal.core.model.ReminderType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerTest {

    private val now = Instant.parse("2026-08-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private lateinit var context: Context
    private lateinit var database: LutealDatabase
    private lateinit var scheduler: ReminderScheduler

    @Before
    fun setup() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scheduler = ReminderScheduler(context, database.reminderDao(), clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun scheduleSnoozeAndCancel() = runTest {
        val triggerAt = now.plusSeconds(120)
        scheduler.schedule("rem-1", ReminderType.MEDS, triggerAt)

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val shadow = shadowOf(alarmManager)
        assertEquals(triggerAt.toEpochMilli(), shadow.scheduledAlarms.single().getTriggerAtMs())

        scheduler.snooze("rem-1", 10)
        assertEquals(now.toEpochMilli() + 10 * 60_000L, shadow.scheduledAlarms.single().getTriggerAtMs())

        scheduler.cancel("rem-1")
        assertTrue(shadow.scheduledAlarms.isEmpty())
        assertEquals(ReminderStatus.DISMISSED.name, database.reminderDao().get("rem-1")?.status)
    }

    @Test
    fun rescheduleAllRestoresScheduledAlarm() = runTest {
        scheduler.schedule("rem-2", ReminderType.PAIN_FOLLOWUP, now.plusSeconds(90))
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val shadow = shadowOf(alarmManager)
        alarmManager.cancel(shadow.scheduledAlarms.single().operation!!)
        assertTrue(shadow.scheduledAlarms.isEmpty())

        scheduler.rescheduleAll()
        assertEquals(now.plusSeconds(90).toEpochMilli(), shadow.scheduledAlarms.single().getTriggerAtMs())
    }
}
