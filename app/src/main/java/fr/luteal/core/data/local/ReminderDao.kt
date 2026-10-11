package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.luteal.core.data.entity.ReminderEntity

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE status = :status")
    suspend fun byStatus(status: String): List<ReminderEntity>

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}
