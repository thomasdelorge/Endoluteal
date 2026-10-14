package fr.luteal.app.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import fr.luteal.app.R
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.ReminderType
import kotlinx.serialization.json.Json

internal fun buildReminderNotification(
    context: Context,
    reminder: ReminderEntity
): NotificationCompat.Builder {
    val type = runCatching { ReminderType.valueOf(reminder.type) }.getOrDefault(ReminderType.RECAP)
    val payload = runCatching {
        Json { ignoreUnknownKeys = true }.decodeFromString(MedsAlarmPayload.serializer(), reminder.payloadJson)
    }.getOrNull()
    val body = when (type) {
        ReminderType.APPOINTMENT_REPORT -> context.getString(R.string.notif_appointment_report_body)
        else -> payload?.items?.joinToString { it.name }?.takeIf { it.isNotBlank() }
            ?: payload?.name?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.notif_reminder_body)
    }
    val title = when (type) {
        ReminderType.APPOINTMENT_REPORT -> context.getString(R.string.notif_appointment_report_title)
        else -> context.getString(R.string.notif_reminder_title)
    }
    val primaryLabel = when {
        type == ReminderType.APPOINTMENT_REPORT -> R.string.notif_action_appointment_report
        type == ReminderType.PAIN_FOLLOWUP -> R.string.notif_action_pain_check
        payload != null && payload.items.isNotEmpty() -> R.string.notif_action_take_all
        else -> R.string.notif_action_open
    }
    val openIntent = activityPending(context, reminder.id, 0, buildReminderOpenIntent(context, reminder))
    val primaryIntent = if (payload != null && payload.items.isNotEmpty() && type == ReminderType.MEDS) {
        broadcastPending(context, reminder.id, ReminderAlarmReceiver.ACTION_PRIMARY, 1)
    } else {
        activityPending(context, reminder.id, 1, buildReminderOpenIntent(context, reminder))
    }
    return NotificationCompat.Builder(context, type.channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .setContentIntent(openIntent)
        .setDeleteIntent(broadcastPending(context, reminder.id, ReminderAlarmReceiver.ACTION_DISMISS, 4))
        .addAction(0, context.getString(primaryLabel), primaryIntent)
        .addAction(0, context.getString(R.string.notif_action_snooze_10), snoozePending(context, reminder.id, 10))
        .addAction(0, context.getString(R.string.notif_action_snooze_30), snoozePending(context, reminder.id, 30))
}

private fun activityPending(context: Context, id: String, code: Int, intent: Intent): PendingIntent =
    PendingIntent.getActivity(
        context,
        id.hashCode() xor code,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

private fun broadcastPending(context: Context, id: String, action: String, code: Int): PendingIntent {
    val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
        this.action = action
        putExtra(ReminderAlarmReceiver.EXTRA_ID, id)
    }
    return PendingIntent.getBroadcast(
        context,
        id.hashCode() xor code,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun snoozePending(context: Context, id: String, minutes: Int): PendingIntent {
    val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
        action = ReminderAlarmReceiver.ACTION_SNOOZE
        putExtra(ReminderAlarmReceiver.EXTRA_ID, id)
        putExtra(ReminderAlarmReceiver.EXTRA_MINUTES, minutes)
    }
    return PendingIntent.getBroadcast(
        context,
        id.hashCode() xor minutes,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
