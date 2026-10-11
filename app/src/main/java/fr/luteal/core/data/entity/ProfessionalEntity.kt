package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "professionals")
data class ProfessionalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialty: String,
    val customSpecialty: String,
    val place: String,
    val phone: String,
    val notes: String
)
