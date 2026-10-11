package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_intakes",
    foreignKeys = [
        ForeignKey(
            entity = JournalEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class MedicationIntakeEntity(
    @PrimaryKey val eventId: String,
    val medicationId: String,
    val units: Double
)
