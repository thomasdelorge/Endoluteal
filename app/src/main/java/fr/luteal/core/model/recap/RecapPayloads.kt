package fr.luteal.core.model.recap

import kotlinx.serialization.Serializable

@Serializable
data class RecapCyclePayload(
    val phaseOverride: String = "",
    val acceptEstimatedPhase: Boolean = true,
    val hasBleeding: Boolean = false,
    val bleedingIntensity: String = "",
    val hasCervicalMucus: Boolean = false,
    val mucusTexture: String = ""
)

@Serializable
data class RecapZoneRange(
    val zone: String,
    val minIntensity: Int = 0,
    val maxIntensity: Int = 0
)

@Serializable
data class RecapPainPayload(
    val zoneRanges: List<RecapZoneRange> = emptyList(),
    val dysmenorrhea: Int? = null,
    val dyspareunia: Int? = null,
    val dyschezia: Int? = null,
    val dysuria: Int? = null,
    val chronicPelvicPain: Int? = null,
    val notes: String = ""
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
