package fr.luteal.app.notification

import fr.luteal.core.data.repository.MedicationIntakeRepository
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.MedsAlarmItem
import fr.luteal.core.model.meds.CandidateIntake
import fr.luteal.core.model.meds.MedicationSafetyChecker
import fr.luteal.core.model.meds.RecordedIntake
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedsDoseTaker @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: MedicationIntakeRepository,
    private val clock: Clock
) {
    /**
     * Records every dose when none of them raises an alert.
     * Returns false without writing when any alert is present.
     */
    suspend fun takeAll(items: List<MedsAlarmItem>): Boolean {
        if (items.isEmpty()) return false
        val at = clock.instant()
        val catalog = medicationRepository.catalogOnce()
        val history = intakeRepository.historyUntil(at, at.minus(Duration.ofHours(24))).toMutableList()
        val reports = items.map { item ->
            MedicationSafetyChecker.check(
                CandidateIntake(item.medicationId, item.units, at),
                history,
                catalog
            ).also { history += RecordedIntake(item.medicationId, item.units, at) }
        }
        if (reports.any { it.alerts.isNotEmpty() }) return false
        val now = clock.instant()
        items.forEach { item ->
            intakeRepository.record(
                JournalEvent(
                    id = UUID.randomUUID().toString(),
                    type = JournalEventType.MEDICATION_INTAKE,
                    startAt = at,
                    endAt = null,
                    zoneId = ZoneId.systemDefault().id,
                    notes = "",
                    linkedEventId = null,
                    payload = EventPayload.MedicationIntake(dangerAcknowledged = false),
                    marks = emptyList(),
                    createdAt = now,
                    updatedAt = now
                ),
                item.medicationId,
                item.units
            )
        }
        return true
    }
}
