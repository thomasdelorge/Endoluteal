package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.notification.AppointmentReportScheduler
import fr.luteal.core.calendar.CalendarImportEngine
import fr.luteal.core.calendar.ProfessionalSuggestionHistory
import fr.luteal.core.data.local.AppointmentDao
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.core.data.repository.AppointmentRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.ProfessionalRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.professional.Appointment
import fr.luteal.core.model.professional.Professional
import fr.luteal.core.model.professional.ReimbursementStatus
import fr.luteal.core.model.professional.ReportStatus
import fr.luteal.core.model.professional.Specialty
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppointmentEntryViewModel @Inject constructor(
    private val journalEventRepository: JournalEventRepository,
    private val appointmentRepository: AppointmentRepository,
    private val professionalRepository: ProfessionalRepository,
    private val appointmentDao: AppointmentDao,
    private val journalEventDao: JournalEventDao,
    private val appointmentReportScheduler: AppointmentReportScheduler,
    private val clock: Clock
) : ViewModel() {
    val professionals: StateFlow<List<Professional>> = professionalRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getEvent(eventId: String): JournalEvent? = journalEventRepository.get(eventId)

    suspend fun getAppointment(eventId: String): Appointment? = appointmentRepository.get(eventId)

    suspend fun save(
        eventId: String?,
        professionalId: String,
        startAt: Instant,
        endAt: Instant?,
        zoneId: ZoneId,
        title: String,
        location: String,
        description: String,
        report: String,
        feeCents: Int?,
        reimbursementStatus: ReimbursementStatus,
        cpamCents: Int?,
        mutuelleCents: Int?
    ) {
        val now = clock.instant()
        val existing = eventId?.let { journalEventRepository.get(it) }
        val existingAppointment = eventId?.let { appointmentRepository.get(it) }
        val id = eventId ?: UUID.randomUUID().toString()
        val status = if (report.isNotBlank()) ReportStatus.DONE else ReportStatus.PENDING
        journalEventRepository.upsert(
            JournalEvent(
                id = id,
                type = JournalEventType.APPOINTMENT,
                startAt = startAt,
                endAt = endAt,
                zoneId = zoneId.id,
                notes = title,
                linkedEventId = null,
                payload = EventPayload.Appointment,
                marks = emptyList(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        // A manual save marks the title and location as locally edited so the
        // next calendar sync keeps the user's wording instead of the calendar's.
        val titleEdited = existingAppointment?.titleEdited == true || (existing != null && title != existing.notes)
        val locationEdited = existingAppointment?.locationEdited == true ||
            (existingAppointment != null && location != existingAppointment.location)
        appointmentRepository.upsert(
            Appointment(
                eventId = id,
                professionalId = professionalId,
                calendarEventKey = existingAppointment?.calendarEventKey,
                location = location,
                description = description,
                report = report,
                reportStatus = status,
                titleEdited = titleEdited,
                locationEdited = locationEdited,
                feeCents = feeCents,
                reimbursementStatus = reimbursementStatus,
                cpamCents = cpamCents,
                mutuelleCents = mutuelleCents
            )
        )
        appointmentReportScheduler.syncForEvent(id)
    }

    suspend fun saveReport(eventId: String, professionalId: String, report: String) {
        val existing = appointmentRepository.get(eventId) ?: return
        val status = if (report.isNotBlank()) ReportStatus.DONE else ReportStatus.PENDING
        appointmentRepository.upsert(
            existing.copy(
                professionalId = professionalId,
                report = report,
                reportStatus = status
            )
        )
        appointmentReportScheduler.syncForEvent(eventId)
    }

    suspend fun suggestProfessionalId(title: String, location: String): String? {
        val events = journalEventDao.getAllOnce()
            .filter { it.type == JournalEventType.APPOINTMENT.name }
            .associateBy { it.id }
        val history = appointmentDao.allOnce().mapNotNull { entity ->
            val event = events[entity.eventId] ?: return@mapNotNull null
            ProfessionalSuggestionHistory(
                professionalId = entity.professionalId,
                title = event.notes,
                location = entity.location,
                startAtEpochMillis = event.startAt
            )
        }
        return CalendarImportEngine.suggestProfessionalId(title, location, history)
    }

    suspend fun createProfessional(name: String, specialty: Specialty, customSpecialty: String): String {
        val id = UUID.randomUUID().toString()
        professionalRepository.upsert(
            Professional(
                id = id,
                name = name.trim(),
                specialty = specialty,
                customSpecialty = customSpecialty.trim(),
                place = "",
                phone = "",
                notes = ""
            )
        )
        return id
    }

    suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }
}
