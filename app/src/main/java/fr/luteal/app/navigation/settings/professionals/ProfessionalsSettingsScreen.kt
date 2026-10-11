package fr.luteal.app.navigation.settings.professionals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.professional.Specialty

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessionalsSettingsScreen(
    onBack: () -> Unit,
    viewModel: ProfessionalCatalogViewModel = hiltViewModel()
) {
    val professionals by viewModel.professionals.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.professionals_settings_back))
        }
        professionals.forEach { professional ->
            Text(
                "${professional.name} · ${professional.displaySpecialty()}",
                style = MaterialTheme.typography.titleMedium
            )
            if (professional.place.isNotBlank()) {
                Text(professional.place, style = MaterialTheme.typography.bodySmall)
            }
        }
        TextButton(onClick = { showAdd = true }) {
            Text(stringResource(R.string.professional_add))
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var specialty by remember { mutableStateOf(Specialty.GENERAL_PRACTITIONER) }
        var custom by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.professional_add)) },
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
                        viewModel.create(name, specialty, custom)
                        showAdd = false
                    }
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }
}
