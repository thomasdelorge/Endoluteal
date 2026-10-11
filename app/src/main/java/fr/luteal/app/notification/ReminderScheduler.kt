package fr.luteal.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.data.local.ReminderDao
import fr.luteal.core.model.ReminderStatus
import fr.luteal.core.model.ReminderType
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val clock: Clock
) {
    suspend fun schedule(
        id: String,
        type: ReminderType,
        triggerAt: Instant,
        payloadJson: String = "{}",
        eventId: String? = null
    ) {
        reminderDao.upsert(
            ReminderEntity(
                id = id,
                type = type.name,
                triggerAt = triggerAt.toEpochMilli(),
                payloadJson = payloadJson,
                eventId = eventId,
                status = ReminderStatus.SCHEDULED.name
            )
        )
        setAlarm(id, triggerAt.toEpochMilli())
    }

    suspend fun cancel(id: String) {
        reminderDao.updateStatus(id, ReminderStatus.DISMISSED.name)
        cancelAlarm(id)
    }

    suspend fun complete(id: String) {
        reminderDao.updateStatus(id, ReminderStatus.DONE.name)
        cancelAlarm(id)
    }

    suspend fun snooze(id: String, minutes: Int) {
        val existing = reminderDao.get(id) ?: return
        val triggerAt = clock.millis() + minutes * 60_000L
        reminderDao.upsert(
            existing.copy(
                triggerAt = triggerAt,
                status = ReminderStatus.SCHEDULED.name
            )
        )
        setAlarm(id, triggerAt)
    }

    suspend fun markFired(id: String) {
        reminderDao.updateStatus(id, ReminderStatus.FIRED.name)
    }

    suspend fun rescheduleAll() {
        reminderDao.byStatus(ReminderStatus.SCHEDULED.name).forEach { reminder ->
            setAlarm(reminder.id, reminder.triggerAt)
        }
    }

    private fun setAlarm(id: String, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            fireIntent(id)
        )
    }

    private fun cancelAlarm(id: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(fireIntent(id))
    }

    private fun fireIntent(id: String): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_FIRE
            putExtra(ReminderAlarmReceiver.EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
