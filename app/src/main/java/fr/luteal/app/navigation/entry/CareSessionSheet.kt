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
import androidx.compose.material3.Button
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
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.DateTimeField
import fr.luteal.core.designsystem.component.PainScale
import fr.luteal.core.designsystem.component.SheetActions
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.care.CareType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CareSessionSheet(
    initialDate: LocalDate,
    existingEventId: String?,
    viewModel: CareSessionEntryViewModel,
    onDismiss: () -> Unit
) {
    val careTypes by viewModel.careTypes.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    var at by remember {
        mutableStateOf(
            if (initialDate == LocalDate.now(zone)) LocalDateTime.now(zone)
            else initialDate.atTime(LocalDateTime.now(zone).toLocalTime())
        )
    }
    var selectedTypeId by remember { mutableStateOf<String?>(null) }
    var programName by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var painBefore by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var followup by remember { mutableStateOf<CareSaveResult?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(careTypes, existingEventId) {
        if (existingEventId == null && selectedTypeId == null && careTypes.isNotEmpty()) {
            selectedTypeId = careTypes.first().id
        }
    }

    LaunchedEffect(existingEventId, careTypes) {
        val id = existingEventId ?: return@LaunchedEffect
        val event = viewModel.get(id)
        if (event != null) {
            at = LocalDateTime.ofInstant(event.startAt, zone)
            notes = event.notes
            val payload = event.payload as? EventPayload.CareSession
            if (payload != null) {
                selectedTypeId = payload.careTypeId.ifBlank { careTypes.firstOrNull()?.id }
                programName = payload.programName
                durationMinutes = if (payload.durationMinutes > 0) payload.durationMinutes.toString() else ""
                painBefore = payload.painBefore
            }
        }
    }

    val selectedType = careTypes.find { it.id == selectedTypeId } ?: careTypes.firstOrNull()
    LaunchedEffect(selectedType?.id, programName) {
        val type = selectedType ?: return@LaunchedEffect
        if (existingEventId != null) return@LaunchedEffect
        val program = type.programs.find { it.name == programName }
        if (program != null) {
            durationMinutes = program.defaultDurationMinutes.toString()
        }
    }

    if (confirmDelete && existingEventId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.care_delete_title)) },
            text = { Text(stringResource(R.string.care_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.delete(existingEventId)
                        onDismiss()
                    }
                }) { Text(stringResource(R.string.care_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }

    val saved = followup
    if (saved != null) {
        val whenLabel = LocalizedDateFormatter.formatNumericDateTime(
            saved.endAt.plus(java.time.Duration.ofHours(1)).atZone(zone).toLocalDateTime()
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.care_followup_title)) },
            text = { Text(stringResource(R.string.care_followup_body, whenLabel)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.schedulePainFollowup(saved.eventId, saved.endAt)
                    onDismiss()
                }) { Text(stringResource(R.string.care_followup_schedule)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.care_followup_skip)) }
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
            Text(stringResource(R.string.care_title), style = MaterialTheme.typography.titleLarge)
            if (careTypes.isEmpty()) {
                Text(
                    stringResource(R.string.care_empty_types),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(stringResource(R.string.care_type), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                    careTypes.forEach { type ->
                        FilterChip(
                            selected = type.id == (selectedTypeId ?: careTypes.first().id),
                            onClick = {
                                selectedTypeId = type.id
                                if (type.programs.none { it.name == programName }) {
                                    programName = ""
                                }
                            },
                            label = { Text(type.name) }
                        )
                    }
                }
                val type = selectedType
                if (type != null && type.programs.isNotEmpty()) {
                    Text(stringResource(R.string.care_program), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                        type.programs.forEach { program ->
                            FilterChip(
                                selected = program.name == programName,
                                onClick = {
                                    programName = program.name
                                    durationMinutes = program.defaultDurationMinutes.toString()
                                },
                                label = { Text(program.name) }
                            )
                        }
                    }
                }
            }
            DateTimeField(value = at, onValueChange = { at = it })
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it.filter { ch -> ch.isDigit() } },
                label = { Text(stringResource(R.string.care_duration)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.care_pain_before))
            PainScale(value = painBefore, onValueChange = { painBefore = it })
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.symptom_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            val duration = durationMinutes.toIntOrNull() ?: 0
            SheetActions(
                onCancel = onDismiss,
                confirmEnabled = selectedType != null && duration > 0,
                onConfirm = {
                    val type = selectedType ?: return@SheetActions
                    scope.launch {
                        followup = viewModel.save(
                            eventId = existingEventId,
                            careType = type,
                            programName = programName,
                            startAt = at.atZone(zone).toInstant(),
                            durationMinutes = duration,
                            painBefore = painBefore,
                            zoneId = zone,
                            notes = notes
                        )
                    }
                }
            )
            if (existingEventId != null) {
                TextButton(onClick = { confirmDelete = true }) {
                    Text(stringResource(R.string.care_delete))
                }
            }
        }
    }
}
