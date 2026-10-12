package fr.luteal.app.notification

import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.repository.DailyRecapRepository
import fr.luteal.core.model.RecapAlarmPayload
import fr.luteal.core.model.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class RecapReminderScheduler @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
    private val dailyRecapRepository: DailyRecapRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val json = Json { encodeDefaults = true }

    suspend fun reconcile(now: LocalDateTime = LocalDateTime.now()) {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        if (!prefs.isNotificationsEnabled || !prefs.isDailyCheckInEnabled) {
            reminderScheduler.cancel(DAILY_RECAP_REMINDER_ID)
            return
        }
        val targetTime = runCatching { LocalTime.parse(prefs.dailyCheckInTime) }
            .getOrDefault(LocalTime.of(21, 0))
        val today = now.toLocalDate()
        val complete = dailyRecapRepository.isFullyComplete(today)
        val targetDateTime = NotificationScheduler.calculateDailyCheckInTarget(
            targetTime = targetTime,
            now = now,
            hasEntryToday = complete
        )
        val zone = ZoneId.systemDefault()
        reminderScheduler.schedule(
            id = DAILY_RECAP_REMINDER_ID,
            type = ReminderType.RECAP,
            triggerAt = targetDateTime.atZone(zone).toInstant(),
            payloadJson = json.encodeToString(RecapAlarmPayload(date = today.toString()))
        )
    }

    companion object {
        const val DAILY_RECAP_REMINDER_ID = "endoluteal-daily-recap"
    }
}
