package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = JournalEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("professionalId")]
)
data class AppointmentEntity(
    @PrimaryKey val eventId: String,
    val professionalId: String,
    val calendarEventKey: String?,
    val location: String,
    val report: String,
    val reportStatus: String,
    val feeCents: Int? = null,
    val reimbursementStatus: String = "",
    val cpamCents: Int? = null,
    val mutuelleCents: Int? = null
)
