package fr.luteal.app.navigation.settings.care

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType

@Composable
fun CareSettingsScreen(
    onBack: () -> Unit,
    viewModel: CareCatalogViewModel = hiltViewModel()
) {
    val types by viewModel.types.collectAsStateWithLifecycle()
    val seedOffered by viewModel.seedOffered.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<CareType?>(null) }
    var newTypeName by remember { mutableStateOf("") }
    var showNewType by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.care_settings_back))
        }
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
            TextButton(onClick = { editing = type }) {
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

    editing?.let { type ->
        var programName by remember(type.id) { mutableStateOf("") }
        var programMinutes by remember(type.id) { mutableStateOf("20") }
        AlertDialog(
            onDismissRequest = { editing = null },
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
                        editing = null
                    }
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }
}
