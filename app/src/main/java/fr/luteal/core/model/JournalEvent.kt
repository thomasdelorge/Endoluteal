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
    val painTypes: List<String>
)

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
