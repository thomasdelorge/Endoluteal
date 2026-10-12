package fr.luteal.app.navigation.settings.meds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.DateField
import fr.luteal.core.designsystem.component.SheetActions
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TreatmentPlanEditor(
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
