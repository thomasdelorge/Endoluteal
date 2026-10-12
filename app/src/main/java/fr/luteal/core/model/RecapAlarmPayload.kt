package fr.luteal.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RecapAlarmPayload(
    val date: String = "",
    /** When set (YYYY-MM), opens the monthly inspired questionnaire instead of the daily recap. */
    val period: String = ""
)
