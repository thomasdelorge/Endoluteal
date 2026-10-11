package fr.luteal.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.MedicationIntakeRepository
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.model.JournalEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AgendaViewModel @Inject constructor(
    journalEventRepository: JournalEventRepository,
    medicationRepository: MedicationRepository,
    intakeRepository: MedicationIntakeRepository
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
}

fun eventsOnDay(events: List<JournalEvent>, date: LocalDate): List<JournalEvent> =
    events.filter { event -> event.localDate() == date }.sortedBy { it.startAt }

fun eventCountsByDay(events: List<JournalEvent>): Map<LocalDate, Int> =
    events.groupingBy { it.localDate() }.eachCount()

private fun JournalEvent.localDate(): LocalDate {
    val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
    return startAt.atZone(zone).toLocalDate()
}
