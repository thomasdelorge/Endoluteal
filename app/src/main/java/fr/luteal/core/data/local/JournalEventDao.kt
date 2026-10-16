package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import fr.luteal.core.data.entity.JournalEventEntity
import fr.luteal.core.data.entity.PainMarkEntity
import kotlinx.coroutines.flow.Flow

data class JournalEventWithMarks(
    @Embedded val event: JournalEventEntity,
    @Relation(parentColumn = "id", entityColumn = "eventId")
    val marks: List<PainMarkEntity>
)

@Dao
interface JournalEventDao {
    @Transaction
    @Query(
        "SELECT * FROM journal_events WHERE startAt >= :from AND startAt < :to ORDER BY startAt ASC"
    )
    fun observeBetween(from: Long, to: Long): Flow<List<JournalEventWithMarks>>

    @Transaction
    @Query("SELECT * FROM journal_events WHERE linkedEventId = :linkedEventId AND type = 'PAIN_CHECK' LIMIT 1")
    suspend fun getLinkedPainCheck(linkedEventId: String): JournalEventWithMarks?

    @Transaction
    @Query("SELECT * FROM journal_events WHERE type = :type ORDER BY startAt ASC")
    fun observeByType(type: String): Flow<List<JournalEventWithMarks>>

    @Transaction
    @Query("SELECT * FROM journal_events WHERE id = :id LIMIT 1")
    suspend fun get(id: String): JournalEventWithMarks?

    @Query("SELECT * FROM journal_events ORDER BY startAt ASC")
    suspend fun getAllOnce(): List<JournalEventEntity>

    @Query("SELECT * FROM pain_marks ORDER BY id ASC")
    suspend fun getAllMarksOnce(): List<PainMarkEntity>

    @Query("SELECT * FROM journal_events WHERE id = :id LIMIT 1")
    suspend fun getEventOnce(id: String): JournalEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: JournalEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<PainMarkEntity>)

    @Query("DELETE FROM pain_marks WHERE eventId = :eventId")
    suspend fun deleteMarksForEvent(eventId: String)

    @Query("DELETE FROM journal_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("DELETE FROM pain_marks")
    suspend fun deleteAllMarks()

    @Query("DELETE FROM journal_events")
    suspend fun deleteAllEvents()

    @Transaction
    suspend fun upsert(event: JournalEventEntity, marks: List<PainMarkEntity>) {
        insertEvent(event)
        deleteMarksForEvent(event.id)
        if (marks.isNotEmpty()) insertMarks(marks)
    }
}
