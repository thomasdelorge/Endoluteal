package fr.luteal.app.navigation.settings.meds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.app.navigation.meds.MedsViewModel
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.ScreenBack
import fr.luteal.core.designsystem.theme.LutealSpacing

@Composable
fun TreatmentPlansSettingsScreen(
    onBack: () -> Unit,
    viewModel: MedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editor by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        ScreenBack(onClick = onBack)
        Text(stringResource(R.string.meds_plans), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.meds_disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.plans.forEach { row ->
            Text("${row.name} · ${row.plan.timesOfDay.joinToString { it.toString() }}")
            row.nextAt?.let {
                Text(
                    stringResource(R.string.meds_next_slot, LocalizedDateFormatter.formatNumericDateTime(it)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        TextButton(onClick = { editor = !editor }) { Text(stringResource(R.string.meds_add_plan)) }
        if (editor) {
            TreatmentPlanEditor(
                medications = state.medications,
                onCancel = { editor = false },
                onSave = { medicationId, units, times, start, end, hormonal ->
                    viewModel.savePlan(medicationId, units, times, start, end, hormonal)
                    editor = false
                }
            )
        }
    }
}
