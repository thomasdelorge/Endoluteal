package fr.luteal.app.navigation.meds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.designsystem.component.LutealPrimaryButton
import fr.luteal.core.model.meds.SafetyAlert
import fr.luteal.core.model.meds.SafetyAlertType
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.ZoneId

@Composable
fun MedsScreen(
    onTake: (String) -> Unit,
    onLogIntake: () -> Unit,
    viewModel: MedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        Text(stringResource(R.string.meds_disclaimer), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.medications.isNotEmpty()) {
            LutealPrimaryButton(
                text = stringResource(R.string.meds_log_intake),
                onClick = onLogIntake,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(stringResource(R.string.meds_recent), style = MaterialTheme.typography.titleMedium)
        state.history.forEach { row ->
            val last = row.lastAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            val next = row.nextAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            LutealCard {
                Text(row.name, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.meds_last_intake, last))
                Text(stringResource(R.string.meds_next_possible, next), style = MaterialTheme.typography.bodySmall)
                row.alert?.let { alert ->
                    Text(
                        text = medsAlertText(alert, row.ruleMessage),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(LutealSpacing.sm),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                LutealPrimaryButton(
                    text = stringResource(R.string.meds_take),
                    onClick = { onTake(row.medicationId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        TreatmentEffectCard()
    }
}

@Composable
private fun medsAlertText(alert: SafetyAlert, ruleMessage: String?): String {
    val base = when (alert.type) {
        SafetyAlertType.DAILY_MAX_EXCEEDED -> stringResource(
            R.string.meds_alert_daily,
            alert.actualMg ?: 0.0,
            alert.limitMg ?: 0.0
        )
        SafetyAlertType.PER_INTAKE_MAX_EXCEEDED -> stringResource(
            R.string.meds_alert_intake,
            alert.actualMg ?: 0.0,
            alert.limitMg ?: 0.0
        )
        SafetyAlertType.MIN_INTERVAL_NOT_ELAPSED -> stringResource(R.string.meds_alert_interval)
        SafetyAlertType.CLASS_DUPLICATE -> stringResource(R.string.meds_alert_class)
        SafetyAlertType.INTERACTION_RULE -> ruleMessage ?: stringResource(R.string.meds_alert_rule)
    }
    return if (alert.unvalidated) "$base ${stringResource(R.string.meds_alert_unvalidated)}" else base
}
