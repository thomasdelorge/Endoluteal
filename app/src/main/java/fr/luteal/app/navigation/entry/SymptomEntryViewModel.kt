package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.PainMark
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@HiltViewModel
class SymptomEntryViewModel @Inject constructor(
    private val journalEventRepository: JournalEventRepository,
    private val clock: Clock
) : ViewModel() {
    suspend fun get(eventId: String): JournalEvent? = journalEventRepository.get(eventId)

    suspend fun save(
        eventId: String?,
        at: Instant,
        zoneId: ZoneId,
        notes: String,
        marks: List<PainMark>
    ) {
        val now = clock.instant()
        val existing = eventId?.let { journalEventRepository.get(it) }
        val event = JournalEvent(
            id = eventId ?: UUID.randomUUID().toString(),
            type = JournalEventType.SYMPTOM,
            startAt = at,
            endAt = null,
            zoneId = zoneId.id,
            notes = notes,
            linkedEventId = null,
            payload = EventPayload.Symptom,
            marks = marks,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        journalEventRepository.upsert(event)
    }

    suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }
}
