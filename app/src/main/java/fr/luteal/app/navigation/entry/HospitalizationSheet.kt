package fr.luteal.app.navigation.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.DateTimeField
import fr.luteal.core.designsystem.component.PainScale
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.EventPayload
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalizationSheet(
    initialDate: LocalDate,
    existingEventId: String?,
    viewModel: HospitalizationEntryViewModel,
    onDismiss: () -> Unit
) {
    val zone = remember { ZoneId.systemDefault() }
    var admissionAt by remember {
        mutableStateOf(
            if (initialDate == LocalDate.now(zone)) LocalDateTime.now(zone)
            else initialDate.atTime(LocalDateTime.now(zone).toLocalTime())
        )
    }
    var hasDischarge by remember { mutableStateOf(false) }
    var dischargeAt by remember { mutableStateOf(admissionAt.plusDays(1)) }
    var place by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var exams by remember { mutableStateOf(listOf<EventPayload.HospitalExam>()) }
    var meds by remember { mutableStateOf(listOf<EventPayload.HospitalAdministeredMedication>()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(existingEventId) {
        val id = existingEventId ?: return@LaunchedEffect
        val event = viewModel.get(id)
        if (event != null) {
            admissionAt = LocalDateTime.ofInstant(event.startAt, zone)
            hasDischarge = event.endAt != null
            if (event.endAt != null) {
                dischargeAt = LocalDateTime.ofInstant(event.endAt, zone)
            }
            notes = event.notes
            val payload = event.payload as? EventPayload.Hospitalization
            if (payload != null) {
                place = payload.place
                reason = payload.reason
                exams = payload.exams
                meds = payload.administeredMedications
            }
        }
    }

    if (confirmDelete && existingEventId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.hospital_delete_title)) },
            text = { Text(stringResource(R.string.hospital_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.delete(existingEventId)
                        onDismiss()
                    }
                }) { Text(stringResource(R.string.hospital_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
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
            Text(stringResource(R.string.hospital_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.hospital_admission))
            DateTimeField(value = admissionAt, onValueChange = { admissionAt = it })
            RowWithSwitch(
                label = stringResource(R.string.hospital_has_discharge),
                checked = hasDischarge,
                onCheckedChange = { hasDischarge = it }
            )
            if (hasDischarge) {
                Text(stringResource(R.string.hospital_discharge))
                DateTimeField(value = dischargeAt, onValueChange = { dischargeAt = it })
            }
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text(stringResource(R.string.hospital_place)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.hospital_reason)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.hospital_exams), style = MaterialTheme.typography.titleMedium)
            exams.forEachIndexed { index, exam ->
                ExamEditor(
                    exam = exam,
                    onChange = { updated ->
                        exams = exams.toMutableList().also { it[index] = updated }
                    },
                    onRemove = {
                        exams = exams.filterIndexed { i, _ -> i != index }
                    }
                )
            }
            TextButton(onClick = {
                exams = exams + EventPayload.HospitalExam()
            }) { Text(stringResource(R.string.hospital_add_exam)) }

            Text(stringResource(R.string.hospital_medications), style = MaterialTheme.typography.titleMedium)
            meds.forEachIndexed { index, med ->
                MedEditor(
                    med = med,
                    onChange = { updated ->
                        meds = meds.toMutableList().also { it[index] = updated }
                    },
                    onRemove = {
                        meds = meds.filterIndexed { i, _ -> i != index }
                    }
                )
            }
            TextButton(onClick = {
                meds = meds + EventPayload.HospitalAdministeredMedication()
            }) { Text(stringResource(R.string.hospital_add_med)) }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.symptom_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = {
                    scope.launch {
                        val endInstant = if (hasDischarge) dischargeAt.atZone(zone).toInstant() else null
                        viewModel.save(
                            eventId = existingEventId,
                            startAt = admissionAt.atZone(zone).toInstant(),
                            endAt = endInstant,
                            zoneId = zone,
                            notes = notes,
                            payload = EventPayload.Hospitalization(
                                place = place,
                                reason = reason,
                                exams = exams,
                                administeredMedications = meds
                            )
                        )
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.meds_save)) }
            if (existingEventId != null) {
                TextButton(onClick = { confirmDelete = true }) {
                    Text(stringResource(R.string.hospital_delete))
                }
            }
        }
    }
}

@Composable
private fun RowWithSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ExamEditor(
    exam: EventPayload.HospitalExam,
    onChange: (EventPayload.HospitalExam) -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        OutlinedTextField(
            value = exam.name,
            onValueChange = { onChange(exam.copy(name = it)) },
            label = { Text(stringResource(R.string.hospital_exam_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = exam.date,
            onValueChange = { onChange(exam.copy(date = it)) },
            label = { Text(stringResource(R.string.hospital_exam_date)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = exam.result,
            onValueChange = { onChange(exam.copy(result = it)) },
            label = { Text(stringResource(R.string.hospital_exam_result)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = onRemove) { Text(stringResource(R.string.hospital_remove_row)) }
    }
}

@Composable
private fun MedEditor(
    med: EventPayload.HospitalAdministeredMedication,
    onChange: (EventPayload.HospitalAdministeredMedication) -> Unit,
    onRemove: () -> Unit
) {
    var pain by remember(med) { mutableStateOf(med.painAfter) }
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        OutlinedTextField(
            value = med.freeText,
            onValueChange = { onChange(med.copy(freeText = it)) },
            label = { Text(stringResource(R.string.hospital_med_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = med.administeredAt,
            onValueChange = { onChange(med.copy(administeredAt = it)) },
            label = { Text(stringResource(R.string.hospital_med_time)) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.hospital_med_pain_after))
        PainScale(value = pain, onValueChange = {
            pain = it
            onChange(med.copy(painAfter = it))
        })
        TextButton(onClick = onRemove) { Text(stringResource(R.string.hospital_remove_row)) }
    }
}
