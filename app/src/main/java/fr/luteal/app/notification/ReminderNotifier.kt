package fr.luteal.app.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.model.ReminderType
import javax.inject.Inject
import javax.inject.Singleton

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
        val notification = buildReminderNotification(context, reminder).build()
        NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
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
