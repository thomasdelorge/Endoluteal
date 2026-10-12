package fr.luteal.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(
    value: LocalDateTime,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val timeLabel = remember(value) {
        LocalizedDateFormatter.formatTime(value.toLocalTime())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        OutlinedButton(onClick = { showDate = true }, modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    R.string.datetime_field_date
                ) + " " + LocalizedDateFormatter.formatNumericDate(value.toLocalDate())
            )
        }
        OutlinedButton(onClick = { showTime = true }, modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.datetime_field_time) + " " + timeLabel)
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            onValueChange(
                                value.withYear(date.year)
                                    .withMonth(date.monthValue)
                                    .withDayOfMonth(date.dayOfMonth)
                            )
                        }
                        showDate = false
                    }
                ) {
                    Text(stringResource(R.string.calendar_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTime) {
        TimePickerDialog(
            initial = value.toLocalTime(),
            onConfirm = {
                onValueChange(value.withHour(it.hour).withMinute(it.minute))
                showTime = false
            },
            onDismiss = { showTime = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    value: LocalTime,
    onValueChange: (LocalTime) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showTime by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { showTime = true }, modifier = modifier.fillMaxWidth()) {
        val timeText = LocalizedDateFormatter.formatTime(value)
        Text(if (label.isBlank()) timeText else "$label $timeText")
    }
    if (showTime) {
        TimePickerDialog(
            initial = value,
            onConfirm = {
                onValueChange(it)
                showTime = false
            },
            onDismiss = { showTime = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListField(
    times: List<LocalTime>,
    onTimesChange: (List<LocalTime>) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        times.forEachIndexed { index, time ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TimeField(
                    value = time,
                    onValueChange = { updated ->
                        onTimesChange(times.toMutableList().also { it[index] = updated })
                    },
                    label = "",
                    modifier = Modifier.weight(1f)
                )
                if (times.size > 1) {
                    IconButton(onClick = { onTimesChange(times.filterIndexed { i, _ -> i != index }) }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.meds_remove_time)
                        )
                    }
                }
            }
        }
        TextButton(
            onClick = {
                val next = times.lastOrNull()?.plusHours(1) ?: LocalTime.of(8, 0)
                onTimesChange(times + next)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.meds_add_time))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.calendar_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = { TimePicker(state = state) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDate by remember { mutableStateOf(false) }
    val shown = value?.let(LocalizedDateFormatter::formatNumericDate)
    OutlinedButton(onClick = { showDate = true }, modifier = modifier.fillMaxWidth()) {
        Text(if (shown == null) label else "$label $shown")
    }
    if (showDate) {
        val initial = (value ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        showDate = false
                    }
                ) {
                    Text(stringResource(R.string.calendar_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
