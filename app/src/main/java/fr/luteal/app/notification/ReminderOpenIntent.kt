package fr.luteal.app.notification

import android.content.Context
import android.content.Intent
import fr.luteal.app.MainActivity
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.RecapAlarmPayload
import fr.luteal.core.model.ReminderType
import java.time.LocalDate
import kotlinx.serialization.json.Json

fun buildReminderOpenIntent(context: Context, reminder: ReminderEntity): Intent {
    val type = runCatching { ReminderType.valueOf(reminder.type) }.getOrDefault(ReminderType.RECAP)
    return Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(ReminderAlarmReceiver.EXTRA_COMPLETE_REMINDER_ID, reminder.id)
        when (type) {
            ReminderType.APPOINTMENT_REPORT -> {
                reminder.eventId?.takeIf { it.isNotBlank() }?.let {
                    putExtra(ReminderAlarmReceiver.EXTRA_OPEN_APPOINTMENT_REPORT, it)
                }
            }
            ReminderType.PAIN_FOLLOWUP -> {
                reminder.eventId?.let { putExtra(ReminderAlarmReceiver.EXTRA_OPEN_PAIN_EVENT, it) }
            }
            ReminderType.MEDS -> {
                val payload = decodeMedsPayload(reminder.payloadJson)
                val medicationId = payload?.medicationId
                    ?: payload?.items?.firstOrNull()?.medicationId
                if (!medicationId.isNullOrBlank()) {
                    putExtra(ReminderAlarmReceiver.EXTRA_OPEN_MEDICATION, medicationId)
                }
            }
            ReminderType.RECAP -> {
                val payload = decodeRecapPayload(reminder.payloadJson)
                val period = payload?.period?.takeIf { it.isNotBlank() }
                if (period != null) {
                    putExtra(ReminderAlarmReceiver.EXTRA_OPEN_MONTHLY_PERIOD, period)
                } else {
                    val date = payload?.date?.takeIf { it.isNotBlank() } ?: LocalDate.now().toString()
                    putExtra(ReminderAlarmReceiver.EXTRA_OPEN_RECAP_DATE, date)
                }
            }
            ReminderType.CARE_FOLLOWUP -> Unit
        }
    }
}

private fun decodeMedsPayload(json: String): MedsAlarmPayload? =
    runCatching {
        Json { ignoreUnknownKeys = true }.decodeFromString(MedsAlarmPayload.serializer(), json)
    }.getOrNull()

private fun decodeRecapPayload(json: String): RecapAlarmPayload? =
    runCatching {
        Json { ignoreUnknownKeys = true }.decodeFromString(RecapAlarmPayload.serializer(), json)
    }.getOrNull()
