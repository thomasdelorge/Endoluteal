package fr.luteal.app.navigation.meds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.common.LocalizedDateFormatter
import fr.luteal.core.designsystem.theme.LutealSpacing
import java.time.ZoneId

@Composable
fun MedsScreen(
    onTake: (String) -> Unit,
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
        Text(stringResource(R.string.meds_recent), style = MaterialTheme.typography.titleMedium)
        state.history.forEach { row ->
            val last = row.lastAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            val next = row.nextAt?.atZone(zone)?.toLocalDateTime()?.let(LocalizedDateFormatter::formatNumericDateTime).orEmpty()
            Text("${row.name} · ${stringResource(R.string.meds_last_intake, last)}")
            Text(stringResource(R.string.meds_next_possible, next), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onTake(row.medicationId) }) { Text(stringResource(R.string.meds_take)) }
        }
        TreatmentEffectCard()
    }
}
