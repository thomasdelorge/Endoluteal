package fr.luteal.app.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.luteal.app.EntrySaveState
import fr.luteal.app.LutealViewModel
import fr.luteal.app.R
import fr.luteal.app.navigation.entry.MedicationIntakeSheet
import fr.luteal.app.navigation.entry.MedicationIntakeViewModel
import fr.luteal.app.navigation.entry.PainCheckSheet
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.ObservationCatalog
import java.time.LocalDate

@Composable
fun LutealMainScaffold(
    pendingImportJson: String? = null,
    pendingPainEventId: String? = null,
    pendingMedicationId: String? = null,
    onPendingImportConsumed: () -> Unit = {},
    onPendingReminderConsumed: () -> Unit = {},
    viewModel: LutealViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val agendaViewModel: AgendaViewModel = hiltViewModel()
    val intakeViewModel: MedicationIntakeViewModel = hiltViewModel()
    val events by agendaViewModel.events.collectAsStateWithLifecycle()
    val intakeLabels by agendaViewModel.intakeLabels.collectAsStateWithLifecycle()
    var selectedDestination by rememberSaveable { mutableStateOf(LutealDestination.AGENDA) }
    var editorRequest by remember { mutableStateOf<EditorRequest?>(null) }
    var intakeDate by remember { mutableStateOf<LocalDate?>(null) }
    var intakeMedicationId by remember { mutableStateOf<String?>(null) }
    var painCheckEventId by remember { mutableStateOf<String?>(null) }
    var showBackfillDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val saveError = stringResource(R.string.save_error)
    val saveSuccess = stringResource(R.string.save_success_local)

    LaunchedEffect(pendingImportJson, uiState.preferences.hasCompletedOnboarding) {
        val json = pendingImportJson ?: return@LaunchedEffect
        if (!uiState.preferences.hasCompletedOnboarding) return@LaunchedEffect

        selectedDestination = LutealDestination.SETTINGS
        settingsViewModel.inspectBackupJson(json)
        onPendingImportConsumed()
    }

    LaunchedEffect(pendingPainEventId, pendingMedicationId) {
        if (pendingPainEventId != null) {
            selectedDestination = LutealDestination.AGENDA
            painCheckEventId = pendingPainEventId
            onPendingReminderConsumed()
        } else if (pendingMedicationId != null) {
            selectedDestination = LutealDestination.AGENDA
            intakeMedicationId = pendingMedicationId
            intakeDate = uiState.today
            onPendingReminderConsumed()
        }
    }

    LaunchedEffect(uiState.operationFailed) {
        if (uiState.operationFailed) {
            snackbarHostState.showSnackbar(saveError)
            viewModel.clearOperationError()
        }
    }

    LaunchedEffect(uiState.entrySaveState) {
        if (uiState.entrySaveState == EntrySaveState.SUCCESS) {
            editorRequest = null
            snackbarHostState.showSnackbar(saveSuccess)
            viewModel.clearEntrySaveState()
        }
    }

    if (uiState.isInitializing) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { }
    } else if (!uiState.preferences.hasCompletedOnboarding) {
        OnboardingScreen(
            onComplete = { role, focusMap, ageBandId ->
                viewModel.completeOnboarding(role, focusMap, ageBandId)
            }
        )
    } else {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        LutealDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == selectedDestination,
                                onClick = { selectedDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(destination.labelRes),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
                        when (selectedDestination) {
                            LutealDestination.AGENDA -> AgendaScreen(
                                state = uiState,
                                events = events,
                                intakeLabels = intakeLabels,
                                onSelectDate = {
                                    viewModel.clearEntrySaveState()
                                    editorRequest = EditorRequest(it, startPeriodIntent = false)
                                },
                                onRecordMedication = { date ->
                                    intakeMedicationId = null
                                    intakeDate = date
                                },
                                onEditCycle = { cycleId, newStartDate ->
                                    viewModel.editCycleStartDate(cycleId, newStartDate)
                                },
                                onDeleteCycle = { cycleId ->
                                    viewModel.deleteCycle(cycleId)
                                },
                                onBackfillCycle = { showBackfillDialog = true }
                            )
                            LutealDestination.TIMELINE -> ComingSoonScreen(R.string.nav_timeline)
                            LutealDestination.MEDICATIONS -> ComingSoonScreen(R.string.nav_medications)
                            LutealDestination.SETTINGS -> SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }

    if (showBackfillDialog) {
        BackfillCycleDialog(
            existingCycleStarts = uiState.cycles.map { it.startDate }.toSet(),
            onConfirm = { startDate ->
                viewModel.addBackfilledCycle(startDate)
                showBackfillDialog = false
            },
            onDismiss = { showBackfillDialog = false }
        )
    }

    editorRequest?.let { req ->
        DailyEntrySheet(
            date = req.date,
            existingEntry = uiState.entries.firstOrNull { it.date == req.date },
            previousDayEntry = uiState.entries.firstOrNull { it.date == req.date.minusDays(1) },
            existingBiomarker = uiState.biomarkers.firstOrNull { it.date == req.date },
            currentCycle = uiState.currentCycle,
            offeredSymptomIds = ObservationCatalog.symptomIdsFor(
                uiState.preferences.declaredContexts
            ),
            startPeriodIntent = req.startPeriodIntent,
            isSaving = uiState.entrySaveState == EntrySaveState.SAVING,
            saveFailed = uiState.entrySaveState == EntrySaveState.FAILED,
            onDismiss = {
                viewModel.clearEntrySaveState()
                editorRequest = null
            },
            onSave = viewModel::saveEntry
        )
    }

    intakeDate?.let { date ->
        MedicationIntakeSheet(
            initialDate = date,
            initialMedicationId = intakeMedicationId,
            viewModel = intakeViewModel,
            onDismiss = {
                intakeDate = null
                intakeMedicationId = null
            }
        )
    }

    painCheckEventId?.let { eventId ->
        PainCheckSheet(
            linkedEventId = eventId,
            viewModel = intakeViewModel,
            onDismiss = { painCheckEventId = null }
        )
    }
}

private data class EditorRequest(
    val date: LocalDate,
    val startPeriodIntent: Boolean
)

private enum class LutealDestination(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    AGENDA(R.string.nav_agenda, Icons.Rounded.CalendarMonth),
    TIMELINE(R.string.nav_timeline, Icons.Rounded.Timeline),
    MEDICATIONS(R.string.nav_medications, Icons.Rounded.Medication),
    SETTINGS(R.string.nav_settings, Icons.Rounded.Settings)
}

@Composable
private fun ComingSoonScreen(@StringRes titleRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(LutealSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        ScreenHeader(
            title = stringResource(titleRes),
            subtitle = stringResource(R.string.screen_coming_soon_title)
        )
        Text(
            text = stringResource(R.string.screen_coming_soon_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
