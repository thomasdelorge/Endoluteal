package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questionnaire_results")
data class QuestionnaireResultEntity(
    @PrimaryKey val id: String,
    val period: String,
    val answersJson: String,
    val score: Int,
    val completedAtEpochMillis: Long
)
