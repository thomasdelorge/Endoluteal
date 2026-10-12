package fr.luteal.app.navigation.settings.professionals

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import fr.luteal.core.designsystem.component.ScreenBack
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.professional.Professional
import fr.luteal.core.model.professional.Specialty

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessionalsSettingsScreen(
    onBack: () -> Unit,
    viewModel: ProfessionalCatalogViewModel = hiltViewModel()
) {
    val professionals by viewModel.professionals.collectAsStateWithLifecycle()
    var editorTarget by remember { mutableStateOf<Professional?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Professional?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        ScreenBack(onClick = onBack)
        professionals.forEach { professional ->
            Text(
                "${professional.name} · ${professional.displaySpecialty()}",
                style = MaterialTheme.typography.titleMedium
            )
            if (professional.place.isNotBlank()) {
                Text(professional.place, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    editorTarget = professional
                    editorOpen = true
                }) { Text(stringResource(R.string.timeline_edit_appointment)) }
                TextButton(onClick = { pendingDelete = professional }) {
                    Text(stringResource(R.string.meds_delete))
                }
            }
        }
        TextButton(onClick = {
            editorTarget = null
            editorOpen = true
        }) {
            Text(stringResource(R.string.professional_add))
        }
    }

    pendingDelete?.let { professional ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.professional_delete_title)) },
            text = { Text(stringResource(R.string.professional_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(professional.id)
                    pendingDelete = null
                }) { Text(stringResource(R.string.meds_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }

    if (editorOpen) {
        val target = editorTarget
        var name by remember(target?.id) { mutableStateOf(target?.name.orEmpty()) }
        var specialty by remember(target?.id) { mutableStateOf(target?.specialty ?: Specialty.GENERAL_PRACTITIONER) }
        var custom by remember(target?.id) { mutableStateOf(target?.customSpecialty.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editorOpen = false },
            title = {
                Text(
                    stringResource(
                        if (target == null) R.string.professional_add else R.string.professional_edit
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.professional_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                        Specialty.entries.forEach { item ->
                            FilterChip(
                                selected = specialty == item,
                                onClick = { specialty = item },
                                label = { Text(item.label) }
                            )
                        }
                    }
                    if (specialty == Specialty.OTHER) {
                        OutlinedTextField(
                            value = custom,
                            onValueChange = { custom = it },
                            label = { Text(stringResource(R.string.professional_custom_specialty)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val current = target
                        if (current == null) {
                            viewModel.create(name, specialty, custom)
                        } else {
                            viewModel.update(
                                current.copy(
                                    name = name.trim(),
                                    specialty = specialty,
                                    customSpecialty = custom.trim()
                                )
                            )
                        }
                        editorOpen = false
                    }
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }
}
