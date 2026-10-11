package fr.luteal.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.luteal.core.data.local.ReminderDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                    ACTION_PRIMARY -> reminderScheduler.complete(id)
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
    }
}
