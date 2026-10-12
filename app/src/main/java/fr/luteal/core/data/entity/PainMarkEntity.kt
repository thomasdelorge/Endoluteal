package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pain_marks",
    foreignKeys = [
        ForeignKey(
            entity = JournalEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"])
    ]
)
data class PainMarkEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val zone: String,
    val intensity: Int,
    val painTypes: String,
    val pinSide: String? = null,
    val pinX: Float? = null,
    val pinY: Float? = null
)
