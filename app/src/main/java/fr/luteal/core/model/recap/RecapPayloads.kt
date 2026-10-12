package fr.luteal.core.model.recap

import kotlinx.serialization.Serializable

@Serializable
data class RecapCyclePayload(
    val placeholder: Boolean = false
)

@Serializable
data class RecapPainPayload(
    val placeholder: Boolean = false
)

@Serializable
data class RecapPsyPayload(
    val placeholder: Boolean = false
)

@Serializable
data class RecapGeneralPayload(
    val placeholder: Boolean = false
)

@Serializable
data class RecapBowelPayload(
    val placeholder: Boolean = false
)
