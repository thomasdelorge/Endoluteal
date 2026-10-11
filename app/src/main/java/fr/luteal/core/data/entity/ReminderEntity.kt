package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val type: String,
    val triggerAt: Long,
    val payloadJson: String,
    val eventId: String?,
    val status: String
)
