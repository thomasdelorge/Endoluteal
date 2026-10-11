package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.CareTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareTypeDao {
    @Query("SELECT * FROM care_types WHERE archived = 0 ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<CareTypeEntity>>

    @Query("SELECT * FROM care_types ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CareTypeEntity>>

    @Query("SELECT * FROM care_types")
    suspend fun allOnce(): List<CareTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CareTypeEntity)

    @Query("DELETE FROM care_types")
    suspend fun deleteAll()
}
