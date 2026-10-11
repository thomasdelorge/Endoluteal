package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_types")
data class CareTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val programsJson: String,
    val archived: Boolean
)
