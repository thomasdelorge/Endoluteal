package fr.luteal.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.luteal.app.LutealUiState
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.designsystem.component.StatusPill
import fr.luteal.core.designsystem.component.StatusTone
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.CycleEstimateResult
import java.time.temporal.ChronoUnit

@Composable
fun CycleEstimateSection(state: LutealUiState, onBackfillCycle: () -> Unit) {
    LutealCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.estimate_title),
                style = MaterialTheme.typography.titleMedium
            )
            StatusPill(
                text = stringResource(R.string.estimated_label),
                tone = StatusTone.ESTIMATED
            )
        }

        Spacer(Modifier.height(LutealSpacing.xs))

        when (val result = state.estimateResult) {
            is CycleEstimateResult.NeedsMoreHistory -> {
                Text(
                    text = stringResource(R.string.estimate_unavailable_short),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(LutealSpacing.xxs))
                Text(
                    text = stringResource(R.string.estimate_unavailable_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onBackfillCycle) {
                    Text(text = stringResource(R.string.backfill_action))
                }
            }

            is CycleEstimateResult.IntervalsOutOfRange -> {
                Text(
                    text = stringResource(R.string.estimate_out_of_range_short),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(LutealSpacing.xxs))
                Text(
                    text = stringResource(R.string.estimate_out_of_range_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is CycleEstimateResult.Available -> {
                val estimate = result.estimate
                val daysUntilCentral =
                    ChronoUnit.DAYS.between(state.today, estimate.centralDate).toInt()
                val daysUntilEarliest =
                    ChronoUnit.DAYS.between(state.today, estimate.earliestDate).toInt()
                val daysPastWindow =
                    ChronoUnit.DAYS.between(estimate.latestDate, state.today).toInt()
                val windowDays =
                    ChronoUnit.DAYS.between(estimate.earliestDate, estimate.latestDate)
                        .toInt() + 1

                Text(
                    text = stringResource(
                        R.string.estimate_target_headline,
                        LocalizedDateFormatter.formatNumericDate(estimate.centralDate)
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(LutealSpacing.xxs))
                Text(
                    text = stringResource(
                        R.string.estimate_window_subhead,
                        LocalizedDateFormatter.formatNumericDate(estimate.earliestDate),
                        LocalizedDateFormatter.formatNumericDate(estimate.latestDate)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(LutealSpacing.xxs))
                Text(
                    text = when {
                        daysUntilCentral > 0 ->
                            pluralStringResource(
                                R.plurals.estimate_days_remaining,
                                daysUntilCentral,
                                daysUntilCentral
                            )
                        daysPastWindow > 0 ->
                            pluralStringResource(
                                R.plurals.estimate_past_window,
                                daysPastWindow,
                                daysPastWindow
                            )
                        else -> stringResource(R.string.estimate_in_progress)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (daysUntilEarliest > 0) {
                    val centralOffsetDays =
                        ChronoUnit.DAYS.between(estimate.earliestDate, estimate.centralDate).toInt()
                    Spacer(Modifier.height(LutealSpacing.md))
                    EstimateWindowTrack(
                        leadDays = daysUntilEarliest,
                        windowDays = windowDays,
                        centralOffsetDays = centralOffsetDays,
                        latestLabel = LocalizedDateFormatter.formatNumericDate(estimate.latestDate)
                    )
                }

                Spacer(Modifier.height(LutealSpacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xxs)) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.estimate_basis,
                            estimate.cycleCount,
                            estimate.cycleCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.estimate_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EstimateWindowTrack(
    leadDays: Int,
    windowDays: Int,
    centralOffsetDays: Int,
    latestLabel: String
) {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.estimate_window_a11y, windowDays)
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .semantics { contentDescription = description }
        ) {
            Box(
                modifier = Modifier
                    .weight(leadDays.toFloat())
                    .fillMaxHeight()
                    .background(scheme.outlineVariant)
            )
            Box(
                modifier = Modifier
                    .weight(windowDays.toFloat())
                    .fillMaxHeight()
                    .background(scheme.tertiary)
            ) {
                val fraction = if (windowDays > 0) {
                    (centralOffsetDays + 0.5f) / windowDays.toFloat()
                } else {
                    0.5f
                }
                val bias = (fraction * 2f - 1f).coerceIn(-1f, 1f)
                Box(
                    modifier = Modifier
                        .align(BiasAlignment(horizontalBias = bias, verticalBias = 0f))
                        .size(6.dp)
                        .background(scheme.onTertiary, shape = CircleShape)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.today_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = latestLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
