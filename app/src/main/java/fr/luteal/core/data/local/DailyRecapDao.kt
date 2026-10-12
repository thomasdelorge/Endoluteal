package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.DailyRecapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecapDao {
    @Query("SELECT * FROM daily_recaps WHERE date = :date LIMIT 1")
    fun observe(date: String): Flow<DailyRecapEntity?>

    @Query("SELECT * FROM daily_recaps WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DailyRecapEntity?

    @Query("SELECT * FROM daily_recaps")
    suspend fun allOnce(): List<DailyRecapEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyRecapEntity)

    @Query("DELETE FROM daily_recaps")
    suspend fun deleteAll()
}
