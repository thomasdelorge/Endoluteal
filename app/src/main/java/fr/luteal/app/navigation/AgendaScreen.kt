package fr.luteal.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.platform.testTag
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WaterDrop
import fr.luteal.core.model.Cycle
import fr.luteal.core.model.CyclePhase
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.luteal.app.LutealUiState
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.CalendarLegendCard
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.designsystem.component.LutealPrimaryButton
import fr.luteal.core.designsystem.component.MonthCalendarGrid
import fr.luteal.core.designsystem.component.StatusPill
import fr.luteal.core.designsystem.component.StatusTone
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.BiomarkerObservation
import fr.luteal.core.model.CervicalMucusSensation
import fr.luteal.core.model.CervicalMucusTexture
import fr.luteal.core.model.PhaseCertainty
import fr.luteal.core.model.BleedingIntensity
import fr.luteal.core.model.CycleEstimateResult
import fr.luteal.core.model.DailyEntry
import fr.luteal.core.model.MonthCalendarProjectionCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.Month
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

@Composable
fun AgendaScreen(
    state: LutealUiState,
    events: List<JournalEvent> = emptyList(),
    intakeLabels: Map<String, String> = emptyMap(),
    onSelectDate: (LocalDate) -> Unit,
    onRecordMedication: (LocalDate) -> Unit = {},
    onRecordSymptom: (LocalDate) -> Unit = {},
    onOpenEvent: (JournalEvent) -> Unit = {},
    onEditCycle: (cycleId: String, newStartDate: LocalDate) -> Unit = { _, _ -> },
    onDeleteCycle: (cycleId: String) -> Unit = {},
    onBackfillCycle: () -> Unit = {}
) {
    val baseMonth = remember { YearMonth.from(state.today) }
    val pagerState = rememberPagerState(initialPage = 1200, pageCount = { 2400 })
    val pagerMonth = remember(pagerState.currentPage) { baseMonth.plusMonths((pagerState.currentPage - 1200).toLong()) }
    var currentMonth by rememberSaveable { mutableStateOf(YearMonth.from(state.today)) }
    LaunchedEffect(pagerState.currentPage) {
        currentMonth = pagerMonth
    }
    var selectedDate by rememberSaveable { mutableStateOf(state.today) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    var cycleToEdit by remember { mutableStateOf<Cycle?>(null) }
    var cycleToDelete by remember { mutableStateOf<Cycle?>(null) }
    val eventCounts = remember(events) { eventCountsByDay(events) }
    val dayEvents = eventsOnDay(events, selectedDate)
    val dailyRecapLabel = stringResource(R.string.agenda_action_daily_recap)
    val medicationLabel = stringResource(R.string.agenda_action_medication)
    val symptomLabel = stringResource(R.string.agenda_action_symptom)
    val displayedEstimate = if (state.hideCycleEstimate) {
        CycleEstimateResult.NeedsMoreHistory
    } else {
        state.estimateResult
    }

    val entriesByDate = remember(state.entries) {
        state.entries.associateBy(DailyEntry::date)
    }

    val calendarProjection = remember(currentMonth, state.today, state.cycles, state.entries, state.biomarkers, displayedEstimate, locale) {
        MonthCalendarProjectionCalculator.project(
            targetMonth = currentMonth,
            today = state.today,
            cycles = state.cycles,
            entries = state.entries,
            estimateResult = displayedEstimate,
            biomarkers = state.biomarkers,
            firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
        )
    }
    val biomarkersByDate = remember(state.biomarkers) {
        state.biomarkers.associateBy(BiomarkerObservation::date)
    }
    val selectedMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val selectedDayProjection = remember(selectedDate, selectedMonth, currentMonth, calendarProjection, state.cycles, state.entries, state.biomarkers, displayedEstimate, locale) {
        if (selectedMonth == currentMonth) {
            calendarProjection.weeks.flatten().firstOrNull { it.date == selectedDate }
        } else {
            MonthCalendarProjectionCalculator.project(
                targetMonth = selectedMonth,
                today = state.today,
                cycles = state.cycles,
                entries = state.entries,
                estimateResult = displayedEstimate,
                biomarkers = state.biomarkers,
                firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            ).weeks.flatten().firstOrNull { it.date == selectedDate }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = currentMonth,
            onDismiss = { showMonthPicker = false },
            onSelectMonth = { target ->
                showMonthPicker = false
                val page = 1200 + ChronoUnit.MONTHS.between(baseMonth, target).toInt()
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LutealSpacing.md,
            vertical = LutealSpacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.md)
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.agenda_title),
                subtitle = stringResource(R.string.agenda_subtitle)
            )
        }

        item {
            if (state.hideCycleEstimate) {
                Text(stringResource(R.string.agenda_hormonal_hidden))
            } else {
                CycleEstimateSection(state = state, onBackfillCycle = onBackfillCycle)
            }
        }

        item {
                MonthNavigationBar(
                    currentMonth = currentMonth,
                    isCurrentMonthToday = currentMonth == YearMonth.from(state.today),
                    onPreviousMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    onNextMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    onJumpToToday = {
                        val todayMonth = YearMonth.from(state.today)
                        val targetPage = 1200 + ChronoUnit.MONTHS.between(baseMonth, todayMonth).toInt()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                        selectedDate = state.today
                    },
                    onTitleClick = { showMonthPicker = true }
                )
            }

            if (calendarProjection.recordedPeriodDaysCount > 0) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.calendar_month_summary_period_days,
                                calendarProjection.recordedPeriodDaysCount
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = LutealSpacing.sm, vertical = LutealSpacing.xs)
                        )
                    }
                }
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pageMonth = baseMonth.plusMonths((page - 1200).toLong())
                    val pageProjection = remember(pageMonth, state.today, state.cycles, state.entries, state.biomarkers, displayedEstimate, locale) {
                        MonthCalendarProjectionCalculator.project(
                            targetMonth = pageMonth,
                            today = state.today,
                            cycles = state.cycles,
                            entries = state.entries,
                            estimateResult = displayedEstimate,
                            biomarkers = state.biomarkers,
                            firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
                        )
                    }
                    MonthCalendarGrid(
                        projection = pageProjection,
                        selectedDate = selectedDate,
                        eventCounts = eventCounts,
                        onSelectDate = { date ->
                            selectedDate = date
                            val targetMonth = YearMonth.from(date)
                            if (targetMonth != pageMonth) {
                                val targetPage = 1200 + ChronoUnit.MONTHS.between(baseMonth, targetMonth).toInt()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            }
                        }
                    )
                }
            }

            item {
                CalendarLegendCard()
            }

            val cycleStart = state.cycles.firstOrNull { it.startDate == selectedDate }
            val selectedCycle = state.cycles.firstOrNull { it.startDate <= selectedDate && (it.endDate == null || it.endDate >= selectedDate) }
            item {
                SelectedDayInspectionCard(
                    date = selectedDate,
                    entry = entriesByDate[selectedDate],
                    biomarker = biomarkersByDate[selectedDate],
                    cycle = selectedCycle,
                    cycleDayNumber = selectedDayProjection?.cycleDayNumber,
                    phase = selectedDayProjection?.cyclePhase,
                    phaseCertainty = selectedDayProjection?.phaseCertainty,
                    isToday = selectedDate == state.today,
                    cycleStart = cycleStart,
                    onEditCycle = { cycleToEdit = it },
                    onDeleteCycle = { cycleToDelete = it },
                    onEditOrAdd = { onSelectDate(selectedDate) }
                )
            }
            item {
                DayEventList(
                    events = dayEvents,
                    intakeLabels = intakeLabels,
                    onOpenEvent = onOpenEvent
                )
            }
    }
        AgendaSpeedDial(
            expanded = fabExpanded,
            actions = listOf(
                SpeedDialAction("agenda_action_daily_recap", dailyRecapLabel) {
                    fabExpanded = false
                    onSelectDate(selectedDate)
                },
                SpeedDialAction("agenda_action_medication", medicationLabel) {
                    fabExpanded = false
                    onRecordMedication(selectedDate)
                },
                SpeedDialAction("agenda_action_symptom", symptomLabel) {
                    fabExpanded = false
                    onRecordSymptom(selectedDate)
                }
            ),
            onToggle = { fabExpanded = !fabExpanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LutealSpacing.md)
        )
    }

    cycleToEdit?.let { cycle ->
        EditCycleDialog(
            cycle = cycle,
            existingCycles = state.cycles,
            today = state.today,
            onDismiss = { cycleToEdit = null },
            onConfirm = { newStartDate ->
                onEditCycle(cycle.id, newStartDate)
                cycleToEdit = null
            }
        )
    }

    cycleToDelete?.let { cycle ->
        DeleteCycleConfirmDialog(
            onDismiss = { cycleToDelete = null },
            onConfirm = {
                onDeleteCycle(cycle.id)
                cycleToDelete = null
            }
        )
    }
}

@Composable
private fun DayEventList(
    events: List<JournalEvent>,
    intakeLabels: Map<String, String>,
    onOpenEvent: (JournalEvent) -> Unit
) {
    val children = events.filter { it.linkedEventId != null }.groupBy { it.linkedEventId }
    val roots = events.filter { event ->
        event.linkedEventId == null || events.none { it.id == event.linkedEventId }
    }
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        if (roots.isEmpty()) {
            Text(
                text = stringResource(R.string.agenda_events_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            roots.forEach { event ->
                EventLine(event, intakeLabels[event.id], onClick = { onOpenEvent(event) })
                children[event.id].orEmpty().forEach { child ->
                    EventLine(child, intakeLabels[child.id], nested = true, onClick = { onOpenEvent(child) })
                }
            }
        }
    }
}

@Composable
private fun EventLine(
    event: JournalEvent,
    intakeLabel: String?,
    nested: Boolean = false,
    onClick: () -> Unit = {}
) {
    val typeLabel = stringResource(event.type.labelRes())
    val pain = event.marks.maxOfOrNull { it.intensity }
    val text = buildString {
        if (nested) append("· ")
        append(typeLabel)
        if (!intakeLabel.isNullOrBlank()) append(" · ").append(intakeLabel)
        if (pain != null) append(" · ").append(pain)
    }
    Text(
        text = text,
        style = if (nested) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .clickable(onClick = onClick)
            .semantics { contentDescription = text }
    )
    if (event.notes.isNotBlank()) {
        Text(
            text = event.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class SpeedDialAction(
    val tag: String,
    val label: String,
    val onClick: () -> Unit
)

@Composable
private fun AgendaSpeedDial(
    expanded: Boolean,
    actions: List<SpeedDialAction>,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        if (expanded) {
            actions.forEach { action ->
                SmallFloatingActionButton(
                    onClick = action.onClick,
                    modifier = Modifier
                        .testTag(action.tag)
                        .semantics { contentDescription = action.label }
                ) {
                    Text(text = action.label, modifier = Modifier.padding(horizontal = LutealSpacing.sm))
                }
            }
        }
        val fabDescription = stringResource(
            if (expanded) R.string.agenda_fab_close else R.string.agenda_fab_open
        )
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier
                .testTag("agenda_fab")
                .semantics { contentDescription = fabDescription }
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = null
            )
        }
    }
}

private fun JournalEventType.labelRes(): Int = when (this) {
    JournalEventType.MEDICATION_INTAKE -> R.string.event_type_medication_intake
    JournalEventType.PAIN_CHECK -> R.string.event_type_pain_check
    JournalEventType.SYMPTOM -> R.string.event_type_symptom
    JournalEventType.CARE_SESSION -> R.string.event_type_care_session
    JournalEventType.HOSPITALIZATION -> R.string.event_type_hospitalization
    JournalEventType.APPOINTMENT -> R.string.event_type_appointment
}

@Composable
private fun MonthNavigationBar(
    currentMonth: YearMonth,
    isCurrentMonthToday: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onTitleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onTitleClick)
                .padding(vertical = LutealSpacing.xs, horizontal = LutealSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalizedDateFormatter.formatMonthYear(currentMonth.atDay(1), locale),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = stringResource(R.string.calendar_picker_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (!isCurrentMonthToday) {
                IconButton(
                    onClick = onJumpToToday,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Today,
                        contentDescription = stringResource(R.string.calendar_today_button),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.calendar_next_month)
                )
            }
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    var selectedYear by remember(currentMonth) { mutableStateOf(currentMonth.year) }
    var selectedMonthVal by remember(currentMonth) { mutableStateOf(currentMonth.month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.calendar_picker_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LutealSpacing.md)
            ) {
                // Year selector with - and + icon buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear-- },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = stringResource(R.string.calendar_previous_month)
                        )
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { selectedYear++ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.calendar_next_month)
                        )
                    }
                }

                // 3x4 grid of month buttons
                val months = Month.entries
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                        ) {
                            for (col in 0 until 3) {
                                val month = months[row * 3 + col]
                                val isSelected = month == selectedMonthVal
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { selectedMonthVal = month },
                                    shape = MaterialTheme.shapes.small,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = month.getDisplayName(TextStyle.SHORT, locale),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSelectMonth(YearMonth.of(selectedYear, selectedMonthVal)) }
            ) {
                Text(stringResource(R.string.calendar_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedDayInspectionCard(
    date: LocalDate,
    entry: DailyEntry?,
    biomarker: BiomarkerObservation?,
    cycle: Cycle?,
    cycleDayNumber: Int?,
    phase: CyclePhase?,
    phaseCertainty: PhaseCertainty?,
    isToday: Boolean,
    cycleStart: Cycle?,
    onEditCycle: (Cycle) -> Unit,
    onDeleteCycle: (Cycle) -> Unit,
    onEditOrAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasEntryObservations = entry?.hasObservations == true
    val hasBiomarkerObservations = biomarker != null && !biomarker.isEmpty
    val locale = LocalConfiguration.current.locales[0]

    LutealCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xxs)) {
                    Text(
                        text = if (isToday) {
                            stringResource(R.string.journal_today)
                        } else {
                            LocalizedDateFormatter.formatFullDate(date, locale)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isToday) {
                        Text(
                            text = LocalizedDateFormatter.formatFullDate(date, locale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (entry?.bleedingIntensity != null && entry.bleedingIntensity != BleedingIntensity.NONE) {
                    BleedingMark(intensity = entry.bleedingIntensity)
                }
            }

            // Badges row: cycle day number and phase badge
            if (cycleDayNumber != null || phase != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                ) {
                    if (cycleDayNumber != null) {
                        StatusPill(
                            text = stringResource(R.string.inspection_cycle_day, cycleDayNumber),
                            tone = StatusTone.NEUTRAL
                        )
                    }
                    if (phase != null) {
                        val phaseName = journalPhaseLabel(phase)
                        val phaseText = if (phaseCertainty == PhaseCertainty.RECORDED) {
                            stringResource(R.string.inspection_phase_recorded, phaseName)
                        } else {
                            stringResource(R.string.inspection_phase_estimated, phaseName)
                        }
                        val tone = if (phaseCertainty == PhaseCertainty.RECORDED) {
                            StatusTone.RECORDED
                        } else {
                            StatusTone.ESTIMATED
                        }
                        StatusPill(
                            text = phaseText,
                            tone = tone
                        )
                    }
                }
            }

            if (cycleStart != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LutealSpacing.sm, vertical = LutealSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cycle_start_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                            IconButton(
                                onClick = { onEditCycle(cycleStart) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.cycle_action_edit),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCycle(cycleStart) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.cycle_action_delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Bleeding detail
            if (entry?.bleedingIntensity != null && entry.bleedingIntensity != BleedingIntensity.NONE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                ) {
                    Text(
                        text = bleedingLabel(entry.bleedingIntensity),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Biomarkers section
            if (biomarker != null && !biomarker.isEmpty) {
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.xxs)) {
                    biomarker.cervicalFluid?.let { fluid ->
                        if (fluid.hasObservation) {
                            val parts = mutableListOf<String>()
                            fluid.sensation?.let { sensation ->
                                parts.add(sensationLabel(sensation))
                            }
                            fluid.texture?.let { texture ->
                                parts.add(textureLabel(texture))
                            }
                            if (parts.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.inspection_biomarker_fluid, parts.joinToString(", ")),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Symptoms section with FlowRow of individual chips
            if (entry?.symptomIds?.isNotEmpty() == true) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                ) {
                    entry.symptomIds.forEach { symptomId ->
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = symptomDisplayName(symptomId),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (hasEntryObservations) {
                val levels = listOfNotNull(
                    entry.painLevel?.let { stringResource(R.string.level_label_pain) to it },
                    entry.moodLevel?.let { stringResource(R.string.level_label_mood) to it },
                    entry.energyLevel?.let { stringResource(R.string.level_label_energy) to it }
                )
                if (levels.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(LutealSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(LutealSpacing.xs)
                    ) {
                        levels.forEach { (label, level) ->
                            MiniLevel(label = label, level = level)
                        }
                    }
                }

                val trailing = journalEntryTrailingSummary(entry)
                if (trailing.isNotEmpty()) {
                    Text(
                        text = trailing,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (entry.notes.isNotBlank()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (!hasBiomarkerObservations && (entry?.bleedingIntensity == null || entry.bleedingIntensity == BleedingIntensity.NONE)) {
                Text(
                    text = stringResource(R.string.calendar_day_empty_inspection),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val hasAnyData = entry?.hasObservations == true || hasBiomarkerObservations
            LutealPrimaryButton(
                text = when {
                    hasAnyData -> stringResource(R.string.action_edit_today)
                    isToday -> stringResource(R.string.action_log_today)
                    else -> stringResource(R.string.action_add_observation)
                },
                onClick = onEditOrAdd,
                icon = if (hasAnyData) Icons.Rounded.Edit else Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


/**
 * Recorded flow for the day, as a drop that fills with intensity.
 *
 * The intensity name travels with it in the accessible label, so the fill is
 * a second encoding of something already stated rather than the only one.
 */
@Composable
private fun BleedingMark(intensity: BleedingIntensity?) {
    val scheme = MaterialTheme.colorScheme
    val recorded = intensity != null && intensity != BleedingIntensity.NONE
    val description = if (intensity == null) {
        stringResource(R.string.journal_no_bleeding_recorded)
    } else {
        stringResource(R.string.today_bleeding_label, bleedingLabel(intensity))
    }
    val dropSize = when (intensity) {
        BleedingIntensity.HEAVY -> 26.dp
        BleedingIntensity.MEDIUM -> 22.dp
        BleedingIntensity.LIGHT -> 19.dp
        BleedingIntensity.SPOTTING -> 16.dp
        else -> 14.dp
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (recorded) scheme.primaryContainer else scheme.surfaceVariant,
                CircleShape
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (recorded) Icons.Rounded.WaterDrop else Icons.Rounded.Remove,
            contentDescription = null,
            tint = if (recorded) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            modifier = Modifier.size(dropSize)
        )
    }
}

/** A one-to-five level as filled segments, with the label kept visible. */
@Composable
private fun MiniLevel(label: String, level: Int) {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.level_a11y, label, level)
    Row(
        horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = description }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (1..5).forEach { step ->
                Box(
                    modifier = Modifier
                        .size(width = 7.dp, height = 7.dp)
                        .background(
                            if (step <= level) scheme.primary else scheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}


@Composable
private fun journalEntryTrailingSummary(entry: DailyEntry): String {
    val parts = buildList {
        if (entry.symptomIds.isNotEmpty()) {
            add(
                pluralStringResource(
                    R.plurals.journal_summary_other_count,
                    entry.symptomIds.size,
                    entry.symptomIds.size
                )
            )
        }
        if (entry.notes.isNotBlank()) add(stringResource(R.string.journal_summary_private_note))
    }
    return parts.joinToString(separator = " · ")
}

@Composable
private fun bleedingLabel(intensity: BleedingIntensity): String = stringResource(
    when (intensity) {
        BleedingIntensity.NONE -> R.string.bleeding_none
        BleedingIntensity.SPOTTING -> R.string.bleeding_spotting
        BleedingIntensity.LIGHT -> R.string.bleeding_light
        BleedingIntensity.MEDIUM -> R.string.bleeding_medium
        BleedingIntensity.HEAVY -> R.string.bleeding_heavy
    }
)


@Composable
private fun symptomDisplayName(id: String): String = when (id) {
    "cramps" -> stringResource(R.string.symptom_cramps)
    "headache" -> stringResource(R.string.symptom_headache)
    "abdominal_pain" -> stringResource(R.string.symptom_abdominal_pain)
    "backache" -> stringResource(R.string.symptom_backache)
    "muscle_aches" -> stringResource(R.string.symptom_muscle_aches)
    "fatigue" -> stringResource(R.string.symptom_fatigue)
    "sleep_issue" -> stringResource(R.string.symptom_sleep_issue)
    "bloating" -> stringResource(R.string.symptom_bloating)
    "nausea" -> stringResource(R.string.symptom_nausea)
    "digestive_changes" -> stringResource(R.string.symptom_digestive_changes)
    "breast_tenderness" -> stringResource(R.string.symptom_breast_tenderness)
    "mood_changes" -> stringResource(R.string.symptom_mood_changes)
    "anxiety" -> stringResource(R.string.symptom_anxiety)
    "acne" -> stringResource(R.string.symptom_acne)
    "pelvic_pain_outside_period" -> stringResource(R.string.symptom_pelvic_pain_outside_period)
    else -> id
}

@Composable
private fun journalPhaseLabel(phase: CyclePhase): String = stringResource(
    when (phase) {
        CyclePhase.MENSTRUAL -> R.string.phase_menstrual
        CyclePhase.FOLLICULAR -> R.string.phase_follicular
        CyclePhase.OVULATORY -> R.string.phase_ovulatory
        CyclePhase.LUTEAL -> R.string.phase_luteal
    }
)

@Composable
private fun sensationLabel(sensation: CervicalMucusSensation): String = stringResource(
    when (sensation) {
        CervicalMucusSensation.DRY -> R.string.sensation_dry
        CervicalMucusSensation.DAMP -> R.string.sensation_damp
        CervicalMucusSensation.WET -> R.string.sensation_wet
        CervicalMucusSensation.SLIPPERY -> R.string.sensation_slippery
    }
)

@Composable
private fun textureLabel(texture: CervicalMucusTexture): String = stringResource(
    when (texture) {
        CervicalMucusTexture.STICKY -> R.string.texture_sticky
        CervicalMucusTexture.CREAMY -> R.string.texture_creamy
        CervicalMucusTexture.EGG_WHITE -> R.string.texture_egg_white
        CervicalMucusTexture.WATERY -> R.string.texture_watery
    }
)
