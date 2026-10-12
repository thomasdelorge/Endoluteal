package fr.luteal.app.navigation.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.FullScreenEntry
import fr.luteal.core.designsystem.component.ScreenBack
import fr.luteal.core.designsystem.component.SheetActions
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.professional.Specialty
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppointmentReportSheet(
    eventId: String,
    viewModel: AppointmentEntryViewModel,
    onDismiss: () -> Unit
) {
    val professionals by viewModel.professionals.collectAsStateWithLifecycle()
    var selectedProfessionalId by remember { mutableStateOf<String?>(null) }
    var report by remember { mutableStateOf("") }
    var showNewProfessional by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId, professionals) {
        val appointment = viewModel.getAppointment(eventId)
        val event = viewModel.getEvent(eventId)
        if (appointment != null) {
            report = appointment.report
            selectedProfessionalId = appointment.professionalId.takeIf { it.isNotBlank() }
        }
        if (selectedProfessionalId == null && event != null) {
            selectedProfessionalId = viewModel.suggestProfessionalId(event.notes, appointment?.location ?: "")
        }
    }

    if (showNewProfessional) {
        NewProfessionalDialog(
            onDismiss = { showNewProfessional = false },
            onSave = { name, specialty, custom ->
                viewModel.createProfessional(name, specialty, custom)
                showNewProfessional = false
            }
        )
    }

    FullScreenEntry {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            ScreenBack(onClick = onDismiss)
            Text(stringResource(R.string.appointment_report_prompt_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.appointment_report_prompt_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(stringResource(R.string.appointment_professional), style = MaterialTheme.typography.labelLarge)
            if (professionals.isEmpty()) {
                Text(
                    stringResource(R.string.appointment_no_professional),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                    professionals.forEach { professional ->
                        FilterChip(
                            selected = professional.id == selectedProfessionalId,
                            onClick = { selectedProfessionalId = professional.id },
                            label = { Text("${professional.name} · ${professional.displaySpecialty()}") }
                        )
                    }
                }
            }
            TextButton(onClick = { showNewProfessional = true }) {
                Text(stringResource(R.string.appointment_add_professional))
            }
            OutlinedTextField(
                value = report,
                onValueChange = { report = it },
                label = { Text(stringResource(R.string.appointment_report)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            SheetActions(
                onCancel = onDismiss,
                confirmEnabled = selectedProfessionalId != null,
                onConfirm = {
                    val professionalId = selectedProfessionalId ?: return@SheetActions
                    scope.launch {
                        viewModel.saveReport(eventId, professionalId, report)
                        onDismiss()
                    }
                }
            )
        }
    }
}
