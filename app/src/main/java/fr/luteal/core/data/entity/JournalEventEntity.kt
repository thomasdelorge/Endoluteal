package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_events",
    indices = [
        Index(value = ["startAt"]),
        Index(value = ["type"])
    ]
)
data class JournalEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val startAt: Long,
    val endAt: Long?,
    val zoneId: String,
    val notes: String,
    val linkedEventId: String?,
    val payloadJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
