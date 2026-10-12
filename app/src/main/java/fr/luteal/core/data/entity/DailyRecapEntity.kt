package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_recaps")
data class DailyRecapEntity(
    @PrimaryKey val date: String,
    val cycleJson: String,
    val painJson: String,
    val psyJson: String,
    val generalJson: String,
    val bowelJson: String,
    val completedStepsJson: String,
    val updatedAtEpochMillis: Long
)
