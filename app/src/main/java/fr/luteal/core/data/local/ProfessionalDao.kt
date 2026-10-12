package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.ProfessionalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfessionalDao {
    @Query("SELECT * FROM professionals ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ProfessionalEntity>>

    @Query("SELECT * FROM professionals")
    suspend fun allOnce(): List<ProfessionalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfessionalEntity)

    @Query("DELETE FROM professionals WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM professionals")
    suspend fun deleteAll()
}
