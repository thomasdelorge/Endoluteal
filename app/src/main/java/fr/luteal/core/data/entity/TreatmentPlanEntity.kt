package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatment_plans",
    indices = [Index(value = ["medicationId"])]
)
data class TreatmentPlanEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val unitsPerDose: Double,
    val timesOfDay: String,
    val startDate: String,
    val endDate: String?,
    val active: Boolean,
    val continuousHormonal: Boolean
)
