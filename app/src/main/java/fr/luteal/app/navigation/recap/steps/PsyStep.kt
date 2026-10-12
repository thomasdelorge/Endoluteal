package fr.luteal.app.navigation.recap.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import fr.luteal.core.model.recap.RecapPsyPayload

@Composable
fun PsyStep(
    hideLibido: Boolean,
    initial: RecapPsyPayload,
    onContinue: (RecapPsyPayload) -> Unit
) {
    var mood by remember(initial) { mutableStateOf(initial.mood) }
    var anxiety by remember(initial) { mutableStateOf(initial.anxiety) }
    var irritability by remember(initial) { mutableStateOf(initial.irritability) }
    var discouragement by remember(initial) { mutableStateOf(initial.discouragement) }
    var overwhelmed by remember(initial) { mutableStateOf(initial.overwhelmed) }
    var isolation by remember(initial) { mutableStateOf(initial.isolation) }
    var libido by remember(initial) { mutableStateOf(initial.libido) }

    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        Text(stringResource(R.string.recap_step_psy), style = MaterialTheme.typography.titleMedium)
        LabeledScale(stringResource(R.string.recap_psy_mood), mood, onValueChange = { mood = it })
        LabeledScale(stringResource(R.string.recap_psy_anxiety), anxiety, onValueChange = { anxiety = it })
        LabeledScale(stringResource(R.string.recap_psy_irritability), irritability, onValueChange = { irritability = it })
        LabeledScale(stringResource(R.string.recap_psy_discouragement), discouragement, onValueChange = { discouragement = it })
        LabeledScale(stringResource(R.string.recap_psy_overwhelmed), overwhelmed, onValueChange = { overwhelmed = it })
        LabeledScale(stringResource(R.string.recap_psy_isolation), isolation, onValueChange = { isolation = it })
        if (!hideLibido) {
            LabeledScale(stringResource(R.string.recap_psy_libido), libido, onValueChange = { libido = it })
        }
        TextButton(
            onClick = {
                onContinue(
                    RecapPsyPayload(
                        mood = mood,
                        anxiety = anxiety,
                        irritability = irritability,
                        discouragement = discouragement,
                        overwhelmed = overwhelmed,
                        isolation = isolation,
                        libido = if (hideLibido) null else libido
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.recap_continue)) }
    }
}
