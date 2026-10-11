package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HospitalizationEntryViewModel @Inject constructor(
    private val journalEventRepository: JournalEventRepository,
    private val clock: Clock
) : ViewModel() {
    suspend fun get(eventId: String): JournalEvent? = journalEventRepository.get(eventId)

    suspend fun save(
        eventId: String?,
        startAt: Instant,
        endAt: Instant?,
        zoneId: ZoneId,
        notes: String,
        payload: EventPayload.Hospitalization
    ) {
        val now = clock.instant()
        val existing = eventId?.let { journalEventRepository.get(it) }
        journalEventRepository.upsert(
            JournalEvent(
                id = eventId ?: UUID.randomUUID().toString(),
                type = JournalEventType.HOSPITALIZATION,
                startAt = startAt,
                endAt = endAt,
                zoneId = zoneId.id,
                notes = notes,
                linkedEventId = null,
                payload = payload,
                marks = emptyList(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }
}
