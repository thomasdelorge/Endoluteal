package fr.luteal.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.theme.LocalPhaseColors
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.BleedingIntensity
import fr.luteal.core.model.CalendarDayProjection
import fr.luteal.core.model.CyclePhase
import fr.luteal.core.model.MonthCalendarProjection
import fr.luteal.core.model.PhaseCertainty
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields

@Composable
fun MonthCalendarGrid(
    projection: MonthCalendarProjection,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    eventCounts: Map<LocalDate, Int> = emptyMap()
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
    ) {
        WeekdayHeaderRow()

        projection.weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        eventCount = eventCounts[day.date] ?: 0,
                        onClick = { onSelectDate(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val locale = LocalConfiguration.current.locales[0]
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val daysOfWeek = (0L..6L).map { firstDayOfWeek.plus(it) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = LutealSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { day ->
            val label = day.getDisplayName(TextStyle.NARROW, locale)
            Text(
                text = label.uppercase(locale),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = day.getDisplayName(TextStyle.FULL, locale)
                    }
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDayProjection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    eventCount: Int = 0
) {
    val phaseColors = LocalPhaseColors.current

    val cellBackground: Color = when {
        day.hasPeriodFlow -> phaseColors.menstrual.container
        day.isEstimatedPeriodTarget -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
        day.isEstimatedPeriodWindow -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        day.isSpottingOnly -> Color.Transparent
        day.cyclePhase == CyclePhase.FOLLICULAR -> phaseColors.follicular.container.copy(alpha = 0.35f)
        day.cyclePhase == CyclePhase.OVULATORY -> phaseColors.ovulatory.container.copy(alpha = 0.45f)
        day.cyclePhase == CyclePhase.LUTEAL -> phaseColors.luteal.container.copy(alpha = 0.35f)
        day.cyclePhase == CyclePhase.MENSTRUAL -> phaseColors.menstrual.container.copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    val cellBorder: BorderStroke? = when {
        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        day.isEstimatedPeriodTarget -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
        day.isEstimatedPeriodWindow -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        day.isSpottingOnly -> BorderStroke(1.dp, phaseColors.menstrual.content.copy(alpha = 0.5f))
        day.isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else -> null
    }

    val textColor: Color = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.outline
        day.hasPeriodFlow -> phaseColors.menstrual.content
        day.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val locale = LocalConfiguration.current.locales[0]
    val cdFormattedDate = LocalizedDateFormatter.formatFullDate(day.date, locale)
    val cdString = buildString {
        append(cdFormattedDate)
        if (day.isToday) {
            append(", ")
            append(stringResource(R.string.calendar_today_button))
        }
        if (day.cycleDayNumber != null) {
            append(", ")
            append(stringResource(R.string.inspection_cycle_day, day.cycleDayNumber))
        }
        when {
            day.hasPeriodFlow -> {
                append(", ")
                val intensityLabel = when (day.bleedingIntensity) {
                    BleedingIntensity.LIGHT -> stringResource(R.string.bleeding_light)
                    BleedingIntensity.MEDIUM -> stringResource(R.string.bleeding_medium)
                    BleedingIntensity.HEAVY -> stringResource(R.string.bleeding_heavy)
                    else -> null
                }
                if (intensityLabel != null) {
                    append("$intensityLabel, ")
                }
                append(stringResource(R.string.calendar_legend_recorded_period))
            }
            day.isSpottingOnly -> {
                append(", ")
                append(stringResource(R.string.bleeding_spotting))
            }
            day.isEstimatedPeriodTarget -> {
                append(", ")
                append(stringResource(R.string.calendar_legend_estimated_period_target))
            }
            day.isEstimatedPeriodWindow -> {
                append(", ")
                append(stringResource(R.string.calendar_legend_estimated_period))
            }
            day.cyclePhase != null -> {
                append(", ")
                val phaseLabel = when (day.cyclePhase) {
                    CyclePhase.MENSTRUAL -> stringResource(R.string.phase_menstrual)
                    CyclePhase.FOLLICULAR -> stringResource(R.string.phase_follicular)
                    CyclePhase.OVULATORY -> stringResource(R.string.phase_ovulatory)
                    CyclePhase.LUTEAL -> stringResource(R.string.phase_luteal)
                }
                if (day.phaseCertainty == PhaseCertainty.RECORDED) {
                    append(stringResource(R.string.inspection_phase_recorded, phaseLabel))
                } else {
                    append(stringResource(R.string.inspection_phase_estimated, phaseLabel))
                }
            }
        }
        if (day.hasObservations) {
            append(", ")
            append(stringResource(R.string.calendar_legend_observation))
        }
        if (day.hasBiomarkers) {
            append(", ")
            append(stringResource(R.string.calendar_legend_biomarkers))
        }
        if (eventCount > 0) {
            append(", ")
            append(stringResource(R.string.calendar_event_dot))
        }
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(LutealSpacing.xs))
            .background(cellBackground)
            .then(
                if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(LutealSpacing.xs))
                else Modifier
            )
            .semantics {
                contentDescription = cdString
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (day.isToday || isSelected || day.hasPeriodFlow) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Indicators below day number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (day.isCycleStart) {
                        CalendarDot(color = phaseColors.menstrual.content, size = 5.dp)
                    }
                    if (day.isSpottingOnly) {
                        CalendarDot(color = phaseColors.menstrual.content.copy(alpha = 0.7f), size = 4.dp)
                    }
                    if (day.hasObservations) {
                        CalendarDot(color = MaterialTheme.colorScheme.secondary, size = 5.dp)
                    }
                    if (day.hasBiomarkers) {
                        CalendarDot(color = MaterialTheme.colorScheme.tertiary, size = 5.dp)
                    }
                    if (eventCount > 0) {
                        CalendarDot(color = MaterialTheme.colorScheme.primary, size = 5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDot(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
