package fr.luteal.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.luteal.core.data.local.ReminderDao
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderDao: ReminderDao
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var reminderNotifier: ReminderNotifier
    @Inject lateinit var medsDoseTaker: MedsDoseTaker
    @Inject lateinit var treatmentPlanScheduler: TreatmentPlanScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        when (intent.action) {
            ACTION_SNOOZE, ACTION_DISMISS, ACTION_PRIMARY ->
                NotificationManagerCompat.from(context).cancel(id.hashCode())
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_SNOOZE -> reminderScheduler.snooze(id, intent.getIntExtra(EXTRA_MINUTES, 10))
                    ACTION_DISMISS -> reminderScheduler.cancel(id)
                    ACTION_PRIMARY -> handleTakeAll(id)
                    else -> {
                        val reminder = reminderDao.get(id) ?: return@launch
                        reminderScheduler.markFired(id)
                        reminderNotifier.show(reminder)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleTakeAll(id: String) {
        val reminder = reminderDao.get(id) ?: return
        val type = runCatching { ReminderType.valueOf(reminder.type) }.getOrNull()
        if (type != ReminderType.MEDS) return
        val payload = runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString(MedsAlarmPayload.serializer(), reminder.payloadJson)
        }.getOrNull()
        if (payload == null || payload.items.isEmpty()) return
        val recorded = medsDoseTaker.takeAll(payload.items)
        if (recorded) {
            reminderScheduler.complete(id)
            treatmentPlanScheduler.sync()
        }
    }

    companion object {
        const val ACTION_FIRE = "fr.luteal.reminder.FIRE"
        const val ACTION_SNOOZE = "fr.luteal.reminder.SNOOZE"
        const val ACTION_DISMISS = "fr.luteal.reminder.DISMISS"
        const val ACTION_PRIMARY = "fr.luteal.reminder.PRIMARY"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_MINUTES = "snooze_minutes"
        const val EXTRA_COMPLETE_REMINDER_ID = "complete_reminder_id"
        const val EXTRA_OPEN_PAIN_EVENT = "open_pain_event"
        const val EXTRA_OPEN_MEDICATION = "open_medication"
        const val EXTRA_OPEN_RECAP_DATE = "open_recap_date"
        const val EXTRA_OPEN_MONTHLY_PERIOD = "open_monthly_period"
        const val EXTRA_OPEN_APPOINTMENT_REPORT = "open_appointment_report"
    }
}
