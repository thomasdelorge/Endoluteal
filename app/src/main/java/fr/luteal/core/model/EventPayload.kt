package fr.luteal.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Type-specific fields of a [JournalEvent]. Subclasses stay minimal until the
 * matching capture screens fill them in.
 */
@Serializable
sealed interface EventPayload {
    @Serializable
    @SerialName("MEDICATION_INTAKE")
    data class MedicationIntake(val dangerAcknowledged: Boolean = false) : EventPayload

    @Serializable
    @SerialName("PAIN_CHECK")
    data object PainCheck : EventPayload

    @Serializable
    @SerialName("SYMPTOM")
    data object Symptom : EventPayload

    @Serializable
    @SerialName("CARE_SESSION")
    data class CareSession(
        val careTypeId: String = "",
        val careTypeName: String = "",
        val programName: String = "",
        val durationMinutes: Int = 0,
        val painBefore: Int? = null
    ) : EventPayload

    @Serializable
    @SerialName("HOSPITALIZATION")
    data object Hospitalization : EventPayload

    @Serializable
    @SerialName("APPOINTMENT")
    data object Appointment : EventPayload

    companion object {
        fun of(type: JournalEventType): EventPayload = when (type) {
            JournalEventType.MEDICATION_INTAKE -> MedicationIntake()
            JournalEventType.PAIN_CHECK -> PainCheck
            JournalEventType.SYMPTOM -> Symptom
            JournalEventType.CARE_SESSION -> CareSession()
            JournalEventType.HOSPITALIZATION -> Hospitalization
            JournalEventType.APPOINTMENT -> Appointment
        }
    }
}

object EventPayloadCodec {
    private val json = Json {
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(payload: EventPayload): String = json.encodeToString(EventPayload.serializer(), payload)

    fun decode(raw: String): EventPayload = json.decodeFromString(EventPayload.serializer(), raw)
}
