package fr.luteal.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.AppointmentRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.MedicationIntakeRepository
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.data.repository.ProfessionalRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AgendaViewModel @Inject constructor(
    journalEventRepository: JournalEventRepository,
    medicationRepository: MedicationRepository,
    intakeRepository: MedicationIntakeRepository,
    appointmentRepository: AppointmentRepository,
    professionalRepository: ProfessionalRepository
) : ViewModel() {
    val events: StateFlow<List<JournalEvent>> = journalEventRepository
        .observeBetween(Instant.EPOCH, Instant.ofEpochMilli(Long.MAX_VALUE))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val intakeLabels: StateFlow<Map<String, String>> = combine(
        medicationRepository.observeCatalog(),
        intakeRepository.observeRows()
    ) { catalog, rows ->
        rows.associate { row ->
            val name = catalog.medications.find { it.id == row.medicationId }?.name ?: row.medicationId
            val units = if (row.units % 1.0 == 0.0) row.units.toInt().toString() else row.units.toString()
            row.eventId to "$name · $units"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val careLabels: StateFlow<Map<String, String>> = events.map { list ->
        list.filter { it.type == JournalEventType.CARE_SESSION }.associate { event ->
            val payload = event.payload as? EventPayload.CareSession
            val label = buildString {
                if (!payload?.careTypeName.isNullOrBlank()) append(payload.careTypeName)
                if (!payload?.programName.isNullOrBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(payload.programName)
                }
            }
            event.id to label
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val appointmentLabels: StateFlow<Map<String, String>> = combine(
        professionalRepository.observeAll(),
        appointmentRepository.observeAll()
    ) { professionals, appointments ->
        appointments.associate { row ->
            val professional = professionals.find { it.id == row.professionalId }
            val label = buildString {
                if (professional != null) {
                    append(professional.name)
                    append(" · ")
                    append(professional.displaySpecialty())
                }
            }
            row.eventId to label
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

fun eventsOnDay(events: List<JournalEvent>, date: LocalDate): List<JournalEvent> =
    events.filter { event -> event.occursOn(date) }.sortedBy { it.startAt }

fun eventCountsByDay(events: List<JournalEvent>): Map<LocalDate, Int> {
    val counts = mutableMapOf<LocalDate, Int>()
    events.forEach { event ->
        event.occurrenceDates().forEach { day ->
            counts[day] = (counts[day] ?: 0) + 1
        }
    }
    return counts
}

fun JournalEvent.occursOn(date: LocalDate): Boolean = occurrenceDates().contains(date)

fun JournalEvent.occurrenceDates(): List<LocalDate> {
    val zone = eventZone()
    val startDate = startAt.atZone(zone).toLocalDate()
    if (type != JournalEventType.HOSPITALIZATION) {
        return listOf(startDate)
    }
    val end = endAt?.atZone(zone)?.toLocalDate() ?: return listOf(startDate)
    if (end.isBefore(startDate)) return listOf(startDate)
    val dates = mutableListOf<LocalDate>()
    var cursor = startDate
    while (!cursor.isAfter(end)) {
        dates.add(cursor)
        cursor = cursor.plusDays(1)
    }
    return dates
}

private fun JournalEvent.eventZone(): ZoneId =
    runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
