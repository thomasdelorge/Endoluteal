package fr.luteal.app.navigation.recap.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.PainScale
import fr.luteal.core.designsystem.component.bodymap.BodyMap
import fr.luteal.core.designsystem.component.bodymap.BodyMapInteraction
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.PainMark
import fr.luteal.core.model.recap.RecapZoneRange
import java.util.UUID

@Composable
fun BodyMapDayRanges(
    ranges: List<RecapZoneRange>,
    onChange: (List<RecapZoneRange>) -> Unit
) {
    var editingZone by remember { mutableStateOf<String?>(null) }
    val marks = remember(ranges) {
        ranges.map { range ->
            PainMark(
                id = UUID.randomUUID().toString(),
                zone = range.zone,
                intensity = range.maxIntensity,
                painTypes = emptyList()
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        BodyMap(
            marks = marks,
            interaction = BodyMapInteraction.Zones,
            onMarksChange = { updated ->
                val next = updated.map { mark ->
                    RecapZoneRange(
                        zone = mark.zone,
                        minIntensity = mark.intensity,
                        maxIntensity = mark.intensity
                    )
                }
                onChange(next)
            }
        )
        ranges.forEach { range ->
            TextButton(onClick = { editingZone = range.zone }) {
                Text("${range.zone} : ${range.minIntensity}–${range.maxIntensity}")
            }
        }
    }
    editingZone?.let { zone ->
        val current = ranges.find { it.zone == zone }
        var min by remember(zone) { mutableIntStateOf(current?.minIntensity ?: 0) }
        var max by remember(zone) { mutableIntStateOf(current?.maxIntensity ?: 0) }
        AlertDialog(
            onDismissRequest = { editingZone = null },
            title = { Text(zone) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                    Text(stringResource(R.string.recap_pain_min))
                    PainScale(value = min, onValueChange = { min = it ?: 0 })
                    Text(stringResource(R.string.recap_pain_max))
                    PainScale(value = max, onValueChange = { max = it ?: 0 })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val adjusted = ranges.map { item ->
                        if (item.zone == zone) {
                            item.copy(
                                minIntensity = min.coerceAtMost(max),
                                maxIntensity = max.coerceAtLeast(min)
                            )
                        } else item
                    }
                    onChange(adjusted)
                    editingZone = null
                }) { Text(stringResource(R.string.meds_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingZone = null }) {
                    Text(stringResource(R.string.intake_cancel))
                }
            }
        )
    }
}
