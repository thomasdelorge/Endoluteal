package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.MedicationIntakeEntity
import kotlinx.coroutines.flow.Flow

data class MedicationIntakeRow(
    val eventId: String,
    val medicationId: String,
    val units: Double,
    val startAt: Long
)

@Dao
interface MedicationIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MedicationIntakeEntity)

    @Query("SELECT * FROM medication_intakes")
    suspend fun allOnce(): List<MedicationIntakeEntity>

    @Query("DELETE FROM medication_intakes")
    suspend fun deleteAll()

    @Query(
        """
        SELECT medication_intakes.eventId AS eventId,
               medication_intakes.medicationId AS medicationId,
               medication_intakes.units AS units,
               journal_events.startAt AS startAt
        FROM medication_intakes
        INNER JOIN journal_events ON journal_events.id = medication_intakes.eventId
        ORDER BY journal_events.startAt ASC
        """
    )
    fun observeRows(): Flow<List<MedicationIntakeRow>>

    @Query(
        """
        SELECT medication_intakes.eventId AS eventId,
               medication_intakes.medicationId AS medicationId,
               medication_intakes.units AS units,
               journal_events.startAt AS startAt
        FROM medication_intakes
        INNER JOIN journal_events ON journal_events.id = medication_intakes.eventId
        WHERE journal_events.startAt > :from AND journal_events.startAt <= :to
        ORDER BY journal_events.startAt ASC
        """
    )
    suspend fun between(from: Long, to: Long): List<MedicationIntakeRow>
}
