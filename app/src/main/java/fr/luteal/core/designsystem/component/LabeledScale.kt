package fr.luteal.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R

/**
 * Five-level recap scale with stable French labels (1 = lowest, 5 = highest).
 */
@Composable
fun LabeledScale(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelLabels = recapScaleLabels()
    ObservationScale(
        label = label,
        supportingText = value?.let { levelLabels[it - 1] } ?: "",
        value = value,
        onValueChange = onValueChange,
        valueDescription = { levelLabels[it - 1] },
        modifier = modifier,
        type = ObservationScaleType.GENERIC
    )
}

@Composable
fun recapScaleLabels(): List<String> = listOf(
    stringResource(R.string.recap_scale_1),
    stringResource(R.string.recap_scale_2),
    stringResource(R.string.recap_scale_3),
    stringResource(R.string.recap_scale_4),
    stringResource(R.string.recap_scale_5)
)
