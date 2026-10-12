package fr.luteal.app.navigation.recap.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.LabeledScale
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.recap.BristolTypeDescriptions
import fr.luteal.core.model.recap.RecapBowelPayload

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BowelStep(
    initial: RecapBowelPayload,
    onContinue: (RecapBowelPayload) -> Unit,
    active: Boolean = true,
    onBindSave: (() -> Unit) -> Unit = {}
) {
    var constipation by remember(initial) { mutableStateOf(initial.constipation) }
    var bristol by remember(initial) { mutableStateOf(initial.bristolType) }
    var bloating by remember(initial) { mutableStateOf(initial.bloating) }

    LaunchedEffect(constipation, bristol, bloating, active) {
        if (!active) return@LaunchedEffect
        onBindSave {
            onContinue(
                RecapBowelPayload(
                    constipation = constipation,
                    bristolType = bristol,
                    bloating = bloating
                )
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        Text(stringResource(R.string.recap_step_bowel), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.recap_bowel_constipation_question), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
            FilterChip(
                selected = !constipation,
                onClick = { constipation = false },
                label = { Text(stringResource(R.string.recap_no)) }
            )
            FilterChip(
                selected = constipation,
                onClick = { constipation = true },
                label = { Text(stringResource(R.string.recap_yes)) }
            )
        }
        Text(stringResource(R.string.recap_bowel_stool), style = MaterialTheme.typography.labelLarge)
        BristolTypeScale(value = bristol, onValueChange = { bristol = it })
        LabeledScale(stringResource(R.string.recap_bowel_bloating), bloating, onValueChange = { bloating = it })
    }
}

@Composable
private fun BristolTypeScale(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..7).forEach { type ->
                val selected = value == type
                val container = if (selected) scheme.primaryContainer else scheme.surface
                val labelColor = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                val border = if (selected) {
                    BorderStroke(2.dp, scheme.primary)
                } else {
                    BorderStroke(1.dp, scheme.outlineVariant)
                }
                Surface(
                    selected = selected,
                    onClick = { onValueChange(type.takeUnless { value == type }) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .semantics { role = Role.RadioButton },
                    shape = MaterialTheme.shapes.small,
                    color = container,
                    border = border
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = labelColor
                        )
                    }
                }
            }
        }
        value?.let { type ->
            val resId = BristolTypeDescriptions.descriptionResId(type)
            if (resId != 0) {
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}
