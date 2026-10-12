package fr.luteal.app.navigation.meds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.R
import fr.luteal.core.data.local.MedicationIntakeRow
import fr.luteal.core.data.repository.DailyRecapRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.data.repository.MedicationIntakeRepository
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.model.CyclePhase
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.meds.Medication
import fr.luteal.core.model.recap.DailyRecap
import fr.luteal.core.model.report.BandEffect
import fr.luteal.core.model.report.LevelBand
import fr.luteal.core.model.report.PhaseEffect
import fr.luteal.core.model.report.TreatmentEffectAggregator
import fr.luteal.core.model.report.TreatmentEffectRow
import fr.luteal.core.model.report.TreatmentEffectSummary
import fr.luteal.core.model.report.TreatmentKind
import fr.luteal.core.model.report.TreatmentUse
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TreatmentEffectViewModel @Inject constructor(
    journalEventRepository: JournalEventRepository,
    intakeRepository: MedicationIntakeRepository,
    medicationRepository: MedicationRepository,
    recapRepository: DailyRecapRepository
) : ViewModel() {
    val summary: StateFlow<TreatmentEffectSummary> = combine(
        journalEventRepository.observeBetween(Instant.EPOCH, FAR_FUTURE),
        intakeRepository.observeRows(),
        medicationRepository.observeCatalog(),
        recapRepository.observeAll()
    ) { events, intakes, catalog, recaps ->
        TreatmentEffectAggregator.summarize(
            toUses(events, intakes, catalog.medications, recaps, ZoneId.systemDefault())
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TreatmentEffectSummary(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )

    private companion object {
        val FAR_FUTURE: Instant = Instant.parse("2100-01-01T00:00:00Z")
    }
}

internal fun toUses(
    events: List<JournalEvent>,
    intakes: List<MedicationIntakeRow>,
    medications: List<Medication>,
    recaps: List<DailyRecap>,
    zone: ZoneId
): List<TreatmentUse> {
    val names = medications.associate { it.id to it.name }
    val recapByDay = recaps.associateBy { it.date }
    val checks = events.filter { it.type == JournalEventType.PAIN_CHECK }
    val intakeByEvent = intakes.associateBy { it.eventId }
    return events.mapNotNull { event ->
        val day = event.startAt.atZone(zone).toLocalDate()
        val recap = recapByDay[day]
        val phase = recap?.cycle?.phaseOverride?.takeIf { it.isNotBlank() }
        val sleep = recap?.general?.sleepQuality
        val stress = recap?.general?.stress
        val after = checks.filter { it.linkedEventId == event.id }.flatMap { it.marks.map { mark -> mark.intensity } }
        when (event.type) {
            JournalEventType.MEDICATION_INTAKE -> {
                val intake = intakeByEvent[event.id] ?: return@mapNotNull null
                TreatmentUse(
                    kind = TreatmentKind.MEDICATION,
                    key = intake.medicationId,
                    name = names[intake.medicationId] ?: intake.medicationId,
                    beforeMarks = event.marks.map { it.intensity },
                    afterMarks = after,
                    phase = phase,
                    sleep = sleep,
                    stress = stress
                )
            }
            JournalEventType.CARE_SESSION -> {
                val payload = event.payload as? EventPayload.CareSession ?: return@mapNotNull null
                val name = payload.careTypeName.ifBlank { payload.programName }.ifBlank { "Soin" }
                TreatmentUse(
                    kind = TreatmentKind.CARE,
                    key = payload.careTypeId.ifBlank { name },
                    name = name,
                    beforeMarks = event.marks.map { it.intensity },
                    painBefore = payload.painBefore,
                    afterMarks = after,
                    phase = phase,
                    sleep = sleep,
                    stress = stress
                )
            }
            else -> null
        }
    }
}

@Composable
fun TreatmentEffectCard(viewModel: TreatmentEffectViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    LutealCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(stringResource(R.string.effect_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.effect_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (summary.isEmpty) {
                Text(stringResource(R.string.effect_empty))
            } else {
                EffectSection(stringResource(R.string.effect_medications), summary.medications)
                EffectSection(stringResource(R.string.effect_care), summary.careTypes)
                PhaseSection(summary.byPhase)
                BandSection(stringResource(R.string.effect_by_sleep), summary.bySleep)
                BandSection(stringResource(R.string.effect_by_stress), summary.byStress)
            }
        }
    }
}

@Composable
private fun EffectSection(title: String, rows: List<TreatmentEffectRow>) {
    if (rows.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge)
    rows.forEach { row ->
        Text(
            stringResource(R.string.effect_row, row.name, row.pairedCount, row.meanDrop)
        )
        Text(
            stringResource(R.string.effect_split, row.improved, row.unchanged, row.worse),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhaseSection(rows: List<PhaseEffect>) {
    if (rows.isEmpty()) return
    Text(stringResource(R.string.effect_by_phase), style = MaterialTheme.typography.labelLarge)
    rows.forEach { row ->
        Text(stringResource(R.string.effect_context_row, phaseLabel(row.phase), row.pairedCount, row.meanDrop))
    }
}

@Composable
private fun BandSection(title: String, rows: List<BandEffect>) {
    if (rows.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge)
    rows.forEach { row ->
        val label = if (row.band == LevelBand.LOW) {
            stringResource(R.string.effect_band_low)
        } else {
            stringResource(R.string.effect_band_high)
        }
        Text(stringResource(R.string.effect_context_row, label, row.pairedCount, row.meanDrop))
    }
}

@Composable
private fun phaseLabel(phase: String): String {
    val known = runCatching { CyclePhase.valueOf(phase) }.getOrNull() ?: return phase
    val res = when (known) {
        CyclePhase.MENSTRUAL -> R.string.phase_menstrual
        CyclePhase.FOLLICULAR -> R.string.phase_follicular
        CyclePhase.OVULATORY -> R.string.phase_ovulatory
        CyclePhase.LUTEAL -> R.string.phase_luteal
    }
    return stringResource(res)
}
