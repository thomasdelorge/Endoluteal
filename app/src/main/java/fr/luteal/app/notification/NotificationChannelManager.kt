package fr.luteal.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.app.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_DAILY_CHECKIN = "channel_daily_checkin"
        const val CHANNEL_PERIOD_WINDOW = "channel_period_window"
        const val CHANNEL_LATE_CYCLE = "channel_late_cycle"
        const val CHANNEL_MEDS = "meds"
        const val CHANNEL_PAIN_FOLLOWUP = "pain_followup"
        const val CHANNEL_CARE_FOLLOWUP = "care_followup"
        const val CHANNEL_RECAP = "recap"
        const val CHANNEL_APPOINTMENT_REPORT = "appointment_report"
        const val CHANNEL_BACKUP = "backup"
    }

    fun registerChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_CHECKIN,
                context.getString(R.string.notif_channel_daily_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_daily_desc)
                setShowBadge(true)
            }

            val windowChannel = NotificationChannel(
                CHANNEL_PERIOD_WINDOW,
                context.getString(R.string.notif_channel_window_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_window_desc)
                setShowBadge(true)
            }

            val lateChannel = NotificationChannel(
                CHANNEL_LATE_CYCLE,
                context.getString(R.string.notif_channel_late_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_late_desc)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(dailyChannel, windowChannel, lateChannel) + reminderChannels()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun reminderChannels(): List<NotificationChannel> = listOf(
        channel(CHANNEL_MEDS, R.string.notif_channel_meds, R.string.notif_channel_meds_desc),
        channel(CHANNEL_PAIN_FOLLOWUP, R.string.notif_channel_pain_followup, R.string.notif_channel_pain_followup_desc),
        channel(CHANNEL_CARE_FOLLOWUP, R.string.notif_channel_care_followup, R.string.notif_channel_care_followup_desc),
        channel(CHANNEL_RECAP, R.string.notif_channel_recap, R.string.notif_channel_recap_desc),
        channel(CHANNEL_APPOINTMENT_REPORT, R.string.notif_channel_appointment_report, R.string.notif_channel_appointment_report_desc),
        channel(CHANNEL_BACKUP, R.string.notif_channel_backup, R.string.notif_channel_backup_desc)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun channel(id: String, titleRes: Int, descriptionRes: Int) = NotificationChannel(
        id,
        context.getString(titleRes),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(descriptionRes)
        setShowBadge(true)
    }
}
