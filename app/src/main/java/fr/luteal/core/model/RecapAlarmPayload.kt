package fr.luteal.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RecapAlarmPayload(
    val date: String = ""
)
