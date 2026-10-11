package fr.luteal.app.navigation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.notification.ReminderScheduler
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.MedicationIntakeRepository
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.MedsAlarmPayload
import fr.luteal.core.model.PainMark
import fr.luteal.core.model.ReminderType
import fr.luteal.core.model.meds.CandidateIntake
import fr.luteal.core.model.meds.MedicationCatalog
import fr.luteal.core.model.meds.MedicationSafetyChecker
import fr.luteal.core.model.meds.SafetyReport
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltViewModel
class MedicationIntakeViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: MedicationIntakeRepository,
    private val journalEventRepository: JournalEventRepository,
    private val reminderScheduler: ReminderScheduler,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val clock: Clock
) : ViewModel() {
    val catalog: StateFlow<MedicationCatalog> = medicationRepository.observeCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicationCatalog(emptyList(), emptyList(), emptyList()))

    private val json = Json { encodeDefaults = true }

    suspend fun evaluate(medicationId: String, units: Double, at: Instant): SafetyReport {
        val current = medicationRepository.catalogOnce()
        val history = intakeRepository.historyUntil(at, at.minus(Duration.ofHours(24)))
        return MedicationSafetyChecker.check(
            CandidateIntake(medicationId, units, at),
            history,
            current
        )
    }

    suspend fun saveIntake(
        medicationId: String,
        units: Double,
        at: Instant,
        marks: List<PainMark>,
        dangerAcknowledged: Boolean
    ): SafetyReport {
        val report = evaluate(medicationId, units, at)
        val now = clock.instant()
        val event = JournalEvent(
            id = UUID.randomUUID().toString(),
            type = JournalEventType.MEDICATION_INTAKE,
            startAt = at,
            endAt = null,
            zoneId = ZoneId.systemDefault().id,
            notes = "",
            linkedEventId = null,
            payload = EventPayload.MedicationIntake(dangerAcknowledged),
            marks = marks,
            createdAt = now,
            updatedAt = now
        )
        intakeRepository.record(event, medicationId, units)
        val delay = userPreferencesDataStore.userPreferencesFlow.first().painFollowupDelayMinutes
        if (delay > 0) {
            reminderScheduler.schedule(
                id = UUID.randomUUID().toString(),
                type = ReminderType.PAIN_FOLLOWUP,
                triggerAt = at.plus(Duration.ofMinutes(delay.toLong())),
                eventId = event.id
            )
        }
        return report
    }

    fun scheduleNextDose(medicationId: String, units: Double, at: Instant, name: String) {
        viewModelScope.launch {
            reminderScheduler.schedule(
                id = UUID.randomUUID().toString(),
                type = ReminderType.MEDS,
                triggerAt = at,
                payloadJson = json.encodeToString(MedsAlarmPayload(medicationId, units, name))
            )
        }
    }

    suspend fun savePainCheck(linkedEventId: String?, marks: List<PainMark>) {
        val now = clock.instant()
        journalEventRepository.upsert(
            JournalEvent(
                id = UUID.randomUUID().toString(),
                type = JournalEventType.PAIN_CHECK,
                startAt = now,
                endAt = null,
                zoneId = ZoneId.systemDefault().id,
                notes = "",
                linkedEventId = linkedEventId,
                payload = EventPayload.PainCheck,
                marks = marks,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
