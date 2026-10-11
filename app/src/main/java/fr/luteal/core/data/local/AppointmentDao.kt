package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments")
    fun observeAll(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments")
    suspend fun allOnce(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE eventId = :eventId LIMIT 1")
    suspend fun get(eventId: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppointmentEntity)

    @Query("DELETE FROM appointments")
    suspend fun deleteAll()
}
