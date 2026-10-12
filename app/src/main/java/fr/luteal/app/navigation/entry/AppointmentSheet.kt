package fr.luteal.app.navigation.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import fr.luteal.core.designsystem.component.DateTimeField
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.professional.Specialty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentSheet(
    initialDate: LocalDate,
    existingEventId: String?,
    viewModel: AppointmentEntryViewModel,
    onDismiss: () -> Unit
) {
    val professionals by viewModel.professionals.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    var startAt by remember {
        mutableStateOf(
            if (initialDate == LocalDate.now(zone)) LocalDateTime.now(zone)
            else initialDate.atTime(9, 0)
        )
    }
    var endAt by remember { mutableStateOf(startAt.plusHours(1)) }
    var selectedProfessionalId by remember { mutableStateOf<String?>(null) }
    var importedTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var report by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var showNewProfessional by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(existingEventId, professionals) {
        val id = existingEventId ?: return@LaunchedEffect
        val event = viewModel.getEvent(id)
        val appointment = viewModel.getAppointment(id)
        if (event != null) {
            startAt = LocalDateTime.ofInstant(event.startAt, zone)
            importedTitle = event.notes
            if (event.endAt != null) {
                endAt = LocalDateTime.ofInstant(event.endAt, zone)
            }
        }
        if (appointment != null) {
            selectedProfessionalId = appointment.professionalId.takeIf { it.isNotBlank() }
            location = appointment.location
            report = appointment.report
        }
    }

    if (confirmDelete && existingEventId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.appointment_delete_title)) },
            text = { Text(stringResource(R.string.appointment_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.delete(existingEventId)
                        onDismiss()
                    }
                }) { Text(stringResource(R.string.appointment_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            Text(stringResource(R.string.appointment_title), style = MaterialTheme.typography.titleLarge)
            if (importedTitle.isNotBlank()) {
                Text(importedTitle, style = MaterialTheme.typography.titleMedium)
            }
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
            Text(stringResource(R.string.appointment_start))
            DateTimeField(value = startAt, onValueChange = { startAt = it })
            Text(stringResource(R.string.appointment_end))
            DateTimeField(value = endAt, onValueChange = { endAt = it })
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(R.string.appointment_location)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = report,
                onValueChange = { report = it },
                label = { Text(stringResource(R.string.appointment_report)) },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = {
                    val professionalId = selectedProfessionalId ?: return@TextButton
                    scope.launch {
                        viewModel.save(
                            eventId = existingEventId,
                            professionalId = professionalId,
                            startAt = startAt.atZone(zone).toInstant(),
                            endAt = endAt.atZone(zone).toInstant(),
                            zoneId = zone,
                            location = location,
                            report = report
                        )
                        onDismiss()
                    }
                },
                enabled = selectedProfessionalId != null
            ) { Text(stringResource(R.string.meds_save)) }
            if (existingEventId != null) {
                TextButton(onClick = { confirmDelete = true }) {
                    Text(stringResource(R.string.appointment_delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NewProfessionalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Specialty, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf(Specialty.GYNECOLOGIST) }
    var customSpecialty by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.appointment_new_professional_title)) },
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
                        value = customSpecialty,
                        onValueChange = { customSpecialty = it },
                        label = { Text(stringResource(R.string.professional_custom_specialty)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(name, specialty, customSpecialty)
            }) { Text(stringResource(R.string.meds_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.intake_cancel)) }
        }
    )
}
