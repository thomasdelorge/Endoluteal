package fr.luteal.app.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.app.R
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.ReminderType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun show(reminder: ReminderEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val type = runCatching { ReminderType.valueOf(reminder.type) }.getOrDefault(ReminderType.RECAP)
        val payload = runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString(MedsAlarmPayload.serializer(), reminder.payloadJson)
        }.getOrNull()
        val body = payload?.name?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.notif_reminder_body)
        val primary = if (type == ReminderType.PAIN_FOLLOWUP) {
            R.string.notif_action_pain_check
        } else {
            R.string.notif_action_open
        }
        val notification = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notif_reminder_title))
            .setContentText(body)
            .setAutoCancel(true)
            .addAction(0, context.getString(primary), actionIntent(reminder.id, ReminderAlarmReceiver.ACTION_PRIMARY, 1))
            .addAction(0, context.getString(R.string.notif_action_snooze_10), snoozeIntent(reminder.id, 10))
            .addAction(0, context.getString(R.string.notif_action_snooze_30), snoozeIntent(reminder.id, 30))
            .addAction(0, context.getString(R.string.notif_action_snooze_60), snoozeIntent(reminder.id, 60))
            .addAction(0, context.getString(R.string.notif_action_dismiss), actionIntent(reminder.id, ReminderAlarmReceiver.ACTION_DISMISS, 4))
            .build()
        NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
    }

    private fun snoozeIntent(id: String, minutes: Int): PendingIntent =
        pending(id, ReminderAlarmReceiver.ACTION_SNOOZE, minutes) {
            putExtra(ReminderAlarmReceiver.EXTRA_MINUTES, minutes)
        }

    private fun actionIntent(id: String, action: String, code: Int): PendingIntent =
        pending(id, action, code) {}

    private fun pending(
        id: String,
        action: String,
        code: Int,
        fill: Intent.() -> Unit
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderAlarmReceiver.EXTRA_ID, id)
            fill()
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode() xor code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

val ReminderType.channelId: String
    get() = when (this) {
        ReminderType.MEDS -> NotificationChannelManager.CHANNEL_MEDS
        ReminderType.PAIN_FOLLOWUP -> NotificationChannelManager.CHANNEL_PAIN_FOLLOWUP
        ReminderType.CARE_FOLLOWUP -> NotificationChannelManager.CHANNEL_CARE_FOLLOWUP
        ReminderType.RECAP -> NotificationChannelManager.CHANNEL_RECAP
        ReminderType.APPOINTMENT_REPORT -> NotificationChannelManager.CHANNEL_APPOINTMENT_REPORT
    }
