package fr.luteal.core.model

import java.time.Instant

enum class JournalEventType {
    MEDICATION_INTAKE,
    PAIN_CHECK,
    SYMPTOM,
    CARE_SESSION,
    HOSPITALIZATION,
    APPOINTMENT
}

data class PainMark(
    val id: String,
    val zone: String,
    val intensity: Int,
    val painTypes: List<String>,
    /** `front` or `back`; normalized pin within that half when set. */
    val pinSide: String? = null,
    val pinX: Float? = null,
    val pinY: Float? = null
) {
    val isPin: Boolean = pinSide != null && pinX != null && pinY != null
}

data class JournalEvent(
    val id: String,
    val type: JournalEventType,
    val startAt: Instant,
    val endAt: Instant?,
    val zoneId: String,
    val notes: String,
    val linkedEventId: String?,
    val payload: EventPayload,
    val marks: List<PainMark>,
    val createdAt: Instant,
    val updatedAt: Instant
)
