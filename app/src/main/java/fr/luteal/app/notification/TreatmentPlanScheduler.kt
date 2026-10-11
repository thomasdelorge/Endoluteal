package fr.luteal.app.notification

import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.data.repository.TreatmentPlanRepository
import fr.luteal.core.model.MedsAlarmItem
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.ReminderType
import fr.luteal.core.model.meds.nextGroupedIntake
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class TreatmentPlanScheduler @Inject constructor(
    private val treatmentPlanRepository: TreatmentPlanRepository,
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: ReminderScheduler
) {
    private val json = Json { encodeDefaults = true }

    suspend fun sync(now: LocalDateTime = LocalDateTime.now(), zone: ZoneId = ZoneId.systemDefault()) {
        val next = nextGroupedIntake(treatmentPlanRepository.allOnce(), now)
        if (next == null) {
            reminderScheduler.cancel(REMINDER_ID)
            return
        }
        val names = medicationRepository.catalogOnce().medications.associate { it.id to it.name }
        val payload = MedsAlarmPayload(
            items = next.doses.map { dose ->
                MedsAlarmItem(
                    planId = dose.planId,
                    medicationId = dose.medicationId,
                    units = dose.units,
                    name = names[dose.medicationId] ?: dose.medicationId
                )
            }
        )
        reminderScheduler.schedule(
            id = REMINDER_ID,
            type = ReminderType.MEDS,
            triggerAt = next.at.atZone(zone).toInstant(),
            payloadJson = json.encodeToString(payload)
        )
    }

    companion object {
        const val REMINDER_ID = "treatment-plans-next"
    }
}
