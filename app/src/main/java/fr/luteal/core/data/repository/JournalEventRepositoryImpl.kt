package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.JournalEventEntity
import fr.luteal.core.data.entity.PainMarkEntity
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.core.data.local.JournalEventWithMarks
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.EventPayloadCodec
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.PainMark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalEventRepositoryImpl @Inject constructor(
    private val journalEventDao: JournalEventDao
) : JournalEventRepository {
    override fun observeBetween(from: Instant, to: Instant): Flow<List<JournalEvent>> =
        journalEventDao.observeBetween(from.toEpochMilli(), to.toEpochMilli())
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeByType(type: JournalEventType): Flow<List<JournalEvent>> =
        journalEventDao.observeByType(type.name).map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(id: String): JournalEvent? =
        journalEventDao.get(id)?.toDomain()

    override suspend fun upsert(event: JournalEvent) {
        event.marks.forEach { mark ->
            require(mark.intensity in 0..10) {
                "Pain intensity must be between 0 and 10"
            }
        }
        journalEventDao.upsert(event.toEntity(), event.marks.map { it.toEntity(event.id) })
    }

    override suspend fun delete(id: String) {
        journalEventDao.deleteEvent(id)
    }

    private fun JournalEventWithMarks.toDomain(): JournalEvent {
        val type = JournalEventType.valueOf(event.type)
        return JournalEvent(
            id = event.id,
            type = type,
            startAt = Instant.ofEpochMilli(event.startAt),
            endAt = event.endAt?.let(Instant::ofEpochMilli),
            zoneId = event.zoneId,
            notes = event.notes,
            linkedEventId = event.linkedEventId,
            payload = runCatching { EventPayloadCodec.decode(event.payloadJson) }
                .getOrDefault(EventPayload.of(type)),
            marks = marks.map { it.toDomain() },
            createdAt = Instant.ofEpochMilli(event.createdAt),
            updatedAt = Instant.ofEpochMilli(event.updatedAt)
        )
    }

    private fun JournalEvent.toEntity() = JournalEventEntity(
        id = id,
        type = type.name,
        startAt = startAt.toEpochMilli(),
        endAt = endAt?.toEpochMilli(),
        zoneId = zoneId,
        notes = notes.trim(),
        linkedEventId = linkedEventId,
        payloadJson = EventPayloadCodec.encode(payload),
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )

    private fun PainMarkEntity.toDomain() = PainMark(
        id = id,
        zone = zone,
        intensity = intensity,
        painTypes = painTypes.toStringList()
    )

    private fun PainMark.toEntity(eventId: String) = PainMarkEntity(
        id = id,
        eventId = eventId,
        zone = zone,
        intensity = intensity,
        painTypes = painTypes.toJson()
    )

    private fun List<String>.toJson(): String {
        val array = JSONArray()
        forEach(array::put)
        return array.toString()
    }

    private fun String.toStringList(): List<String> = runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.getString(index) }
    }.getOrDefault(emptyList())
}
