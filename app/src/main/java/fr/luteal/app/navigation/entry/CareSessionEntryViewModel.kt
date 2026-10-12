package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.notification.ReminderScheduler
import fr.luteal.core.data.repository.CareTypeRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.ReminderType
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType
import fr.luteal.core.model.care.CareTypeSeed
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CareSaveResult(val eventId: String, val endAt: Instant)

@HiltViewModel
class CareSessionEntryViewModel @Inject constructor(
    private val journalEventRepository: JournalEventRepository,
    private val careTypeRepository: CareTypeRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) : ViewModel() {
    val careTypes: StateFlow<List<CareType>> = careTypeRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun get(eventId: String): JournalEvent? = journalEventRepository.get(eventId)

    suspend fun save(
        eventId: String?,
        careType: CareType,
        programName: String,
        startAt: Instant,
        durationMinutes: Int,
        painBefore: Int?,
        zoneId: ZoneId,
        notes: String
    ): CareSaveResult {
        val now = clock.instant()
        val existing = eventId?.let { journalEventRepository.get(it) }
        val endAt = startAt.plus(Duration.ofMinutes(durationMinutes.toLong()))
        val id = eventId ?: UUID.randomUUID().toString()
        val event = JournalEvent(
            id = id,
            type = JournalEventType.CARE_SESSION,
            startAt = startAt,
            endAt = endAt,
            zoneId = zoneId.id,
            notes = notes,
            linkedEventId = null,
            payload = EventPayload.CareSession(
                careTypeId = careType.id,
                careTypeName = careType.name,
                programName = programName,
                durationMinutes = durationMinutes,
                painBefore = painBefore
            ),
            marks = emptyList(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        journalEventRepository.upsert(event)
        rememberProgram(careType, programName, durationMinutes)
        return CareSaveResult(id, endAt)
    }

    private suspend fun rememberProgram(careType: CareType, programName: String, durationMinutes: Int) {
        val name = programName.trim()
        if (careType.id != CareTypeSeed.TENS_ID || name.isEmpty()) return
        val current = careTypeRepository.allOnce().find { it.id == careType.id } ?: return
        if (current.programs.any { it.name == name }) return
        careTypeRepository.upsert(
            current.copy(programs = current.programs + CareProgram(name, durationMinutes))
        )
    }

    fun schedulePainFollowup(eventId: String, endAt: Instant) {
        viewModelScope.launch {
            reminderScheduler.schedule(
                id = UUID.randomUUID().toString(),
                type = ReminderType.PAIN_FOLLOWUP,
                triggerAt = endAt.plus(Duration.ofHours(1)),
                eventId = eventId
            )
        }
    }

    suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }
}
