package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "substances")
data class SubstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val drugClass: String,
    val maxDailyMg: Double?,
    val maxPerIntakeMg: Double?,
    val minIntervalMinutes: Int?,
    val validated: Boolean,
    val notes: String
)
