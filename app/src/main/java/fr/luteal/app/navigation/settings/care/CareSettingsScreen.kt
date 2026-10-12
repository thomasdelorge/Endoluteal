package fr.luteal.app.navigation.settings.care

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.designsystem.component.ScreenBack
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType
import fr.luteal.core.model.care.CareTypeSeed

@Composable
fun CareSettingsScreen(
    onBack: () -> Unit,
    viewModel: CareCatalogViewModel = hiltViewModel()
) {
    val types by viewModel.types.collectAsStateWithLifecycle()
    val seedOffered by viewModel.seedOffered.collectAsStateWithLifecycle()
    var editingId by remember { mutableStateOf<String?>(null) }
    var newTypeName by remember { mutableStateOf("") }
    var showNewType by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        ScreenBack(onClick = onBack)
        if (!seedOffered) {
            Text(stringResource(R.string.care_seed_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.care_seed_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = viewModel::acceptSeed) {
                Text(stringResource(R.string.care_seed_accept))
            }
            TextButton(onClick = viewModel::dismissSeed) {
                Text(stringResource(R.string.care_seed_dismiss))
            }
        }
        types.filter { !it.archived }.forEach { type ->
            if (type.id == CareTypeSeed.TENS_ID) {
                Text(type.name, style = MaterialTheme.typography.titleMedium)
                TensProgramsEditor(type = type, viewModel = viewModel)
            } else {
                TextButton(onClick = { editingId = type.id }) {
                    Text(type.name, style = MaterialTheme.typography.titleMedium)
                }
                type.programs.forEach { program ->
                    Text(
                        "· ${program.name} (${program.defaultDurationMinutes} min)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = LutealSpacing.md)
                    )
                }
            }
        }
        TextButton(onClick = { showNewType = true }) {
            Text(stringResource(R.string.care_add_type))
        }
    }

    if (showNewType) {
        AlertDialog(
            onDismissRequest = { showNewType = false },
            title = { Text(stringResource(R.string.care_add_type)) },
            text = {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = { newTypeName = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTypeName.isNotBlank()) {
                        viewModel.addCustomType(newTypeName)
                        newTypeName = ""
                        showNewType = false
                    }
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewType = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }

    types.find { it.id == editingId && it.id != CareTypeSeed.TENS_ID }?.let { type ->
        var programName by remember(type.id) { mutableStateOf("") }
        var programMinutes by remember(type.id) { mutableStateOf("20") }
        AlertDialog(
            onDismissRequest = { editingId = null },
            title = { Text(type.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                    OutlinedTextField(
                        value = programName,
                        onValueChange = { programName = it },
                        label = { Text(stringResource(R.string.care_program)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = programMinutes,
                        onValueChange = { programMinutes = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.care_duration)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = programMinutes.toIntOrNull() ?: return@TextButton
                    if (programName.isNotBlank() && minutes > 0) {
                        viewModel.addProgram(type.id, CareProgram(programName.trim(), minutes))
                        editingId = null
                    }
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingId = null }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TensProgramsEditor(type: CareType, viewModel: CareCatalogViewModel) {
    var newName by remember { mutableStateOf("") }
    var newMinutes by remember { mutableStateOf<Int?>(null) }
    var renaming by remember { mutableStateOf<String?>(null) }
    var renamed by remember { mutableStateOf("") }
    LutealCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(LutealSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
        ) {
            type.programs.forEach { program ->
                if (renaming == program.name) {
                    OutlinedTextField(
                        value = renamed,
                        onValueChange = { renamed = it },
                        label = { Text(stringResource(R.string.care_program_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row {
                        TextButton(onClick = { renaming = null }) {
                            Text(stringResource(R.string.intake_cancel))
                        }
                        TextButton(onClick = {
                            val next = renamed.trim()
                            if (next.isNotEmpty()) {
                                viewModel.renameProgram(
                                    type.id,
                                    program.name,
                                    CareProgram(next, program.defaultDurationMinutes)
                                )
                                renaming = null
                            }
                        }) { Text(stringResource(R.string.meds_save)) }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${program.name} · ${program.defaultDurationMinutes} min",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            renaming = program.name
                            renamed = program.name
                        }) { Text(stringResource(R.string.care_rename_program)) }
                        IconButton(onClick = { viewModel.removeProgram(type.id, program.name) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.meds_delete))
                        }
                    }
                }
            }
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.care_program_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                listOf(
                    30 to R.string.care_tens_duration_30,
                    60 to R.string.care_tens_duration_60,
                    90 to R.string.care_tens_duration_90,
                    120 to R.string.care_tens_duration_120
                ).forEach { (minutes, label) ->
                    FilterChip(
                        selected = newMinutes == minutes,
                        onClick = { newMinutes = minutes },
                        label = { Text(stringResource(label)) }
                    )
                }
            }
            TextButton(onClick = {
                val minutes = newMinutes ?: return@TextButton
                val name = newName.trim()
                if (name.isNotEmpty()) {
                    viewModel.addProgram(type.id, CareProgram(name, minutes))
                    newName = ""
                    newMinutes = null
                }
            }) { Text(stringResource(R.string.care_add_program)) }
        }
    }
}
