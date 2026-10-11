package fr.luteal.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_rules")
data class InteractionRuleEntity(
    @PrimaryKey val id: String,
    val targetAType: String,
    val targetAId: String,
    val targetBType: String,
    val targetBId: String,
    val severity: String,
    val message: String
)
