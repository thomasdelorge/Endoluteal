package fr.luteal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.luteal.app.R

private val PainGreen = Color(0xFF2E7D32)
private val PainRed = Color(0xFFC62828)

fun painIntensityColor(intensity: Int): Color =
    lerp(PainGreen, PainRed, intensity.coerceIn(0, 10) / 10f)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PainScale(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (0..10).forEach { level ->
                PainScaleSegment(
                    level = level,
                    selected = value == level,
                    onClick = { onValueChange(level.takeUnless { value == level }) }
                )
            }
        }
        Text(
            text = stringResource(R.string.pain_scale_anchor_none),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.pain_scale_anchor_moderate),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.pain_scale_anchor_worst),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PainScaleSegment(
    level: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val anchor = when (level) {
        0 -> stringResource(R.string.pain_scale_anchor_none)
        5 -> stringResource(R.string.pain_scale_anchor_moderate)
        10 -> stringResource(R.string.pain_scale_anchor_worst)
        else -> null
    }
    val description = if (anchor != null) {
        stringResource(R.string.pain_scale_value, level, anchor)
    } else {
        stringResource(R.string.pain_scale_value_only, level)
    }
    val color = painIntensityColor(level)
    Column(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .testTag("pain_scale_$level")
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                stateDescription = description
            }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .background(
                color = if (selected) color else color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = level.toString(),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
