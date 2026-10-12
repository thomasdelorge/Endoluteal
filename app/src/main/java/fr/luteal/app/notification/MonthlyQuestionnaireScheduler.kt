package fr.luteal.app.notification

import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.repository.QuestionnaireResultRepository
import fr.luteal.core.model.RecapAlarmPayload
import fr.luteal.core.model.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class MonthlyQuestionnaireScheduler @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
    private val questionnaireResultRepository: QuestionnaireResultRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val json = Json { encodeDefaults = true }

    suspend fun reconcile(now: LocalDateTime = LocalDateTime.now()) {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        if (!prefs.isNotificationsEnabled || !prefs.isDailyCheckInEnabled) {
            reminderScheduler.cancel(MONTHLY_REMINDER_ID)
            return
        }
        val period = YearMonth.from(now)
        if (questionnaireResultRepository.getForPeriod(period) != null) {
            reminderScheduler.cancel(MONTHLY_REMINDER_ID)
            return
        }
        val targetTime = runCatching { LocalTime.parse(prefs.dailyCheckInTime) }
            .getOrDefault(LocalTime.of(21, 0))
        val firstOfMonth = period.atDay(1)
        val targetDateTime = if (now.toLocalDate().isAfter(firstOfMonth)) {
            firstOfMonth.plusMonths(1).atTime(targetTime)
        } else {
            firstOfMonth.atTime(targetTime).let { if (it.isBefore(now)) now.plusMinutes(1) else it }
        }
        val zone = ZoneId.systemDefault()
        reminderScheduler.schedule(
            id = MONTHLY_REMINDER_ID,
            type = ReminderType.RECAP,
            triggerAt = targetDateTime.atZone(zone).toInstant(),
            payloadJson = json.encodeToString(
                RecapAlarmPayload(date = LocalDate.now().toString(), period = period.toString())
            )
        )
    }

    companion object {
        const val MONTHLY_REMINDER_ID = "endoluteal-monthly-questionnaire"
    }
}
