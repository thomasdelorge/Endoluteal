package fr.luteal.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.luteal.app.MainActivity
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

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_SNOOZE -> reminderScheduler.snooze(id, intent.getIntExtra(EXTRA_MINUTES, 10))
                    ACTION_DISMISS -> reminderScheduler.cancel(id)
                    ACTION_PRIMARY -> handlePrimary(context, id)
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

    companion object {
        const val ACTION_FIRE = "fr.luteal.reminder.FIRE"
        const val ACTION_SNOOZE = "fr.luteal.reminder.SNOOZE"
        const val ACTION_DISMISS = "fr.luteal.reminder.DISMISS"
        const val ACTION_PRIMARY = "fr.luteal.reminder.PRIMARY"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_MINUTES = "snooze_minutes"
        const val EXTRA_OPEN_PAIN_EVENT = "open_pain_event"
        const val EXTRA_OPEN_MEDICATION = "open_medication"
    }
}

private suspend fun ReminderAlarmReceiver.handlePrimary(context: Context, id: String) {
    val reminder = reminderDao.get(id) ?: return
    val type = runCatching { ReminderType.valueOf(reminder.type) }.getOrNull()
    reminderScheduler.complete(id)
    when (type) {
        ReminderType.PAIN_FOLLOWUP -> context.openEndoluteal {
            putExtra(ReminderAlarmReceiver.EXTRA_OPEN_PAIN_EVENT, reminder.eventId)
        }
        ReminderType.MEDS -> {
            val payload = runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString(MedsAlarmPayload.serializer(), reminder.payloadJson)
            }.getOrNull()
            val medicationId = payload?.medicationId
            if (!medicationId.isNullOrBlank()) {
                context.openEndoluteal {
                    putExtra(ReminderAlarmReceiver.EXTRA_OPEN_MEDICATION, medicationId)
                }
            }
        }
        else -> Unit
    }
}

private fun Context.openEndoluteal(fill: Intent.() -> Unit) {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        fill()
    }
    startActivity(intent)
}
