package fr.luteal.core.model.care

import kotlinx.serialization.Serializable

@Serializable
data class CareProgram(
    val name: String,
    val defaultDurationMinutes: Int
)

data class CareType(
    val id: String,
    val name: String,
    val programs: List<CareProgram>,
    val archived: Boolean
)
