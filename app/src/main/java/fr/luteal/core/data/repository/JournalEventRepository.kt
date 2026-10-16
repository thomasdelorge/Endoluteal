package fr.luteal.core.data.repository

import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface JournalEventRepository {
    /** Events whose [JournalEvent.startAt] is in `[from, to)`. */
    fun observeBetween(from: Instant, to: Instant): Flow<List<JournalEvent>>

    fun observeByType(type: JournalEventType): Flow<List<JournalEvent>>

    suspend fun get(id: String): JournalEvent?

    /** The pain check reported after a medication intake, if any. */
    suspend fun getLinkedPainCheck(linkedEventId: String): JournalEvent?

    suspend fun upsert(event: JournalEvent)

    suspend fun delete(id: String)
}
