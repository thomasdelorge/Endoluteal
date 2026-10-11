package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.AppointmentRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.ProfessionalRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.professional.Appointment
import fr.luteal.core.model.professional.Professional
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
        location: String,
        report: String
    ) {
        val now = clock.instant()
        val existing = eventId?.let { journalEventRepository.get(it) }
        val id = eventId ?: UUID.randomUUID().toString()
        val status = if (report.isNotBlank()) ReportStatus.DONE else ReportStatus.PENDING
        journalEventRepository.upsert(
            JournalEvent(
                id = id,
                type = JournalEventType.APPOINTMENT,
                startAt = startAt,
                endAt = endAt,
                zoneId = zoneId.id,
                notes = "",
                linkedEventId = null,
                payload = EventPayload.Appointment,
                marks = emptyList(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        appointmentRepository.upsert(
            Appointment(
                eventId = id,
                professionalId = professionalId,
                calendarEventKey = null,
                location = location,
                report = report,
                reportStatus = status
            )
        )
    }

    fun createProfessional(name: String, specialty: Specialty, customSpecialty: String) {
        viewModelScope.launch {
            professionalRepository.upsert(
                Professional(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    specialty = specialty,
                    customSpecialty = customSpecialty.trim(),
                    place = "",
                    phone = "",
                    notes = ""
                )
            )
        }
    }

    suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }
}
