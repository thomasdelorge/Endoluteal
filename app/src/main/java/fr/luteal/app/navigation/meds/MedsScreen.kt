package fr.luteal.app.navigation.meds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MedsScreen(
    onTake: (String) -> Unit,
    viewModel: MedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }
    var editor by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        Text(stringResource(R.string.meds_disclaimer), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.meds_plans), style = MaterialTheme.typography.titleMedium)
        state.plans.forEach { row ->
            Text("${row.name} · ${row.plan.timesOfDay.joinToString { it.toString() }}")
            row.nextAt?.let { Text(stringResource(R.string.meds_next_slot, it.format(formatter)), style = MaterialTheme.typography.bodySmall) }
        }
        TextButton(onClick = { editor = !editor }) { Text(stringResource(R.string.meds_add_plan)) }
        if (editor) {
            PlanEditor(state.medications) { medicationId, units, times, start, end, hormonal ->
                viewModel.savePlan(medicationId, units, times, start, end, hormonal)
                editor = false
            }
        }
        Text(stringResource(R.string.meds_recent), style = MaterialTheme.typography.titleMedium)
        state.history.forEach { row ->
            val last = row.lastAt?.atZone(zone)?.format(formatter).orEmpty()
            val next = row.nextAt?.atZone(zone)?.format(formatter).orEmpty()
            Text("${row.name} · ${stringResource(R.string.meds_last_intake, last)}")
            Text(stringResource(R.string.meds_next_possible, next), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onTake(row.medicationId) }) { Text(stringResource(R.string.meds_take)) }
        }
    }
}

@Composable
private fun PlanEditor(
    medications: List<Pair<String, String>>,
    onSave: (String, Double, List<LocalTime>, LocalDate, LocalDate?, Boolean) -> Unit
) {
    var medicationId by remember { mutableStateOf(medications.firstOrNull()?.first) }
    var units by remember { mutableStateOf("1") }
    var times by remember { mutableStateOf("08:00") }
    var start by remember { mutableStateOf(LocalDate.now().toString()) }
    var end by remember { mutableStateOf("") }
    var hormonal by remember { mutableStateOf(false) }
    medications.forEach { (id, name) ->
        TextButton(onClick = { medicationId = id }) {
            Text(if (id == medicationId) "• $name" else name)
        }
    }
    OutlinedTextField(units, { units = it }, label = { Text(stringResource(R.string.intake_units)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(times, { times = it }, label = { Text(stringResource(R.string.meds_times)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(start, { start = it }, label = { Text(stringResource(R.string.meds_start)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(end, { end = it }, label = { Text(stringResource(R.string.meds_end)) }, modifier = Modifier.fillMaxWidth())
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.meds_hormonal))
        Switch(checked = hormonal, onCheckedChange = { hormonal = it })
    }
    TextButton(onClick = {
        val id = medicationId ?: return@TextButton
        val parsedTimes = times.split(',', ' ').mapNotNull { token ->
            token.trim().takeIf { it.isNotEmpty() }?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        }
        val parsedStart = runCatching { LocalDate.parse(start) }.getOrNull() ?: return@TextButton
        val parsedEnd = end.trim().takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val count = units.replace(',', '.').toDoubleOrNull() ?: return@TextButton
        if (parsedTimes.isEmpty()) return@TextButton
        onSave(id, count, parsedTimes, parsedStart, parsedEnd, hormonal)
    }) { Text(stringResource(R.string.meds_save)) }
}
