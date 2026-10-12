package fr.luteal.app.navigation.meds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.DateField
import fr.luteal.core.designsystem.component.SheetActions
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun MedsScreen(
    onTake: (String) -> Unit,
    viewModel: MedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
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
            row.nextAt?.let {
                Text(
                    stringResource(R.string.meds_next_slot, LocalizedDateFormatter.formatNumericDateTime(it)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        TextButton(onClick = { editor = !editor }) { Text(stringResource(R.string.meds_add_plan)) }
        if (editor) {
            PlanEditor(
                medications = state.medications,
                onCancel = { editor = false },
                onSave = { medicationId, units, times, start, end, hormonal ->
                    viewModel.savePlan(medicationId, units, times, start, end, hormonal)
                    editor = false
                }
            )
        }
        Text(stringResource(R.string.meds_recent), style = MaterialTheme.typography.titleMedium)
        state.history.forEach { row ->
            val last = row.lastAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            val next = row.nextAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            Text("${row.name} · ${stringResource(R.string.meds_last_intake, last)}")
            Text(stringResource(R.string.meds_next_possible, next), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onTake(row.medicationId) }) { Text(stringResource(R.string.meds_take)) }
        }
        TreatmentEffectCard()
    }
}

@Composable
private fun PlanEditor(
    medications: List<Pair<String, String>>,
    onCancel: () -> Unit,
    onSave: (String, Double, List<LocalTime>, LocalDate, LocalDate?, Boolean) -> Unit
) {
    var medicationId by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var units by remember { mutableStateOf("1") }
    var times by remember { mutableStateOf("08:00") }
    var start by remember { mutableStateOf(LocalDate.now()) }
    var end by remember { mutableStateOf<LocalDate?>(null) }
    var hormonal by remember { mutableStateOf(false) }
    val selected = medications.find { it.first == medicationId }
    val filtering = selected == null || query != selected.second
    val options = medications.filter { (_, name) ->
        !filtering || query.isBlank() || name.contains(query, ignoreCase = true)
    }
    val parsedTimes = times.split(',', ' ').mapNotNull { token ->
        token.trim().takeIf { it.isNotEmpty() }?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    }
    val count = units.replace(',', '.').toDoubleOrNull()
    val endDate = end
    val endAfterStart = endDate == null || !endDate.isBefore(start)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                menuOpen = true
                if (selected?.second != it) medicationId = null
            },
            label = { Text(stringResource(R.string.intake_search)) },
            trailingIcon = {
                IconButton(onClick = { menuOpen = !menuOpen }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(R.string.intake_search))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        medicationId = id
                        query = name
                        menuOpen = false
                    }
                )
            }
        }
    }
    OutlinedTextField(units, { units = it }, label = { Text(stringResource(R.string.intake_units)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(times, { times = it }, label = { Text(stringResource(R.string.meds_times)) }, modifier = Modifier.fillMaxWidth())
    DateField(value = start, onValueChange = { start = it }, label = stringResource(R.string.meds_start))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        DateField(
            value = end,
            onValueChange = { end = it },
            label = stringResource(R.string.meds_end),
            modifier = Modifier.weight(1f)
        )
        if (end != null) {
            TextButton(onClick = { end = null }) { Text(stringResource(R.string.meds_clear_end)) }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.meds_hormonal))
        Switch(checked = hormonal, onCheckedChange = { hormonal = it })
    }
    SheetActions(
        onCancel = onCancel,
        confirmEnabled = medicationId != null && count != null && count > 0.0 && parsedTimes.isNotEmpty() && endAfterStart,
        onConfirm = {
            val id = medicationId ?: return@SheetActions
            val unitsPerDose = count ?: return@SheetActions
            if (parsedTimes.isEmpty() || !endAfterStart) return@SheetActions
            onSave(id, unitsPerDose, parsedTimes, start, end, hormonal)
        }
    )
}
