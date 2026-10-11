package fr.luteal.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MedsAlarmPayload(
    val medicationId: String = "",
    val units: Double = 1.0,
    val name: String = "",
    val items: List<MedsAlarmItem> = emptyList()
)

@Serializable
data class MedsAlarmItem(
    val planId: String,
    val medicationId: String,
    val units: Double,
    val name: String
)
