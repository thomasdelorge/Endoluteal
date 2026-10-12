package fr.luteal.app.navigation.recap.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.LabeledScale
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.recap.RecapBowelPayload
import fr.luteal.core.model.recap.BristolTypeDescriptions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BowelStep(
    initial: RecapBowelPayload,
    onContinue: (RecapBowelPayload) -> Unit
) {
    var constipation by remember(initial) { mutableStateOf(initial.constipation) }
    var bristol by remember(initial) { mutableStateOf(initial.bristolType) }
    var bloating by remember(initial) { mutableStateOf(initial.bloating) }

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
        (1..7).forEach { type ->
            FilterChip(
                selected = bristol == type,
                onClick = { bristol = type },
                label = { Text(BristolTypeDescriptions.label(type)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (bristol == type) {
                Text(
                    BristolTypeDescriptions.description(type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LabeledScale(stringResource(R.string.recap_bowel_bloating), bloating, onValueChange = { bloating = it })
        TextButton(
            onClick = {
                onContinue(
                    RecapBowelPayload(
                        constipation = constipation,
                        bristolType = bristol,
                        bloating = bloating
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.recap_continue)) }
    }
}
