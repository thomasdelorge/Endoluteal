package fr.luteal.app.notification

import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.local.AppointmentDao
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.ReminderType
import fr.luteal.core.model.professional.ReportStatus
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class AppointmentReportScheduler @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
    private val appointmentDao: AppointmentDao,
    private val journalEventDao: JournalEventDao,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val clock: Clock
) {
    suspend fun syncForEvent(eventId: String) {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        val reminderId = reminderId(eventId)
        if (!prefs.isNotificationsEnabled) {
            reminderScheduler.cancel(reminderId)
            return
        }
        val appointment = appointmentDao.get(eventId) ?: return
        val status = ReportStatus.fromStored(appointment.reportStatus)
        if (status == ReportStatus.DONE || status == ReportStatus.CANCELLED) {
            reminderScheduler.cancel(reminderId)
            return
        }
        val event = journalEventDao.get(eventId)?.event ?: return
        val end = event.endAt?.let(Instant::ofEpochMilli) ?: Instant.ofEpochMilli(event.startAt)
        // Never notify retroactively: an appointment already finished (e.g. picked
        // up by a calendar sync) no longer triggers a CR reminder.
        if (!end.isAfter(clock.instant())) {
            reminderScheduler.cancel(reminderId)
            return
        }
        reminderScheduler.schedule(
            id = reminderId,
            type = ReminderType.APPOINTMENT_REPORT,
            triggerAt = end,
            payloadJson = "{}",
            eventId = eventId
        )
    }

    suspend fun reconcileAll() {
        journalEventDao.getAllOnce()
            .filter { it.type == JournalEventType.APPOINTMENT.name }
            .forEach { syncForEvent(it.id) }
    }

    companion object {
        fun reminderId(eventId: String): String = "appointment-report:$eventId"
    }
}
