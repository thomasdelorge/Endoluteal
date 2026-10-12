package fr.luteal.app.navigation.recap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.recap.RecapStep
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapWizardSheet(
    date: LocalDate,
    viewModel: RecapWizardViewModel,
    onDismiss: () -> Unit
) {
    LaunchedEffect(date) { viewModel.open(date) }
    val recap by viewModel.recap.collectAsStateWithLifecycle()
    val steps = RecapStep.ordered
    val pagerState = rememberPagerState(
        initialPage = viewModel.initialPageIndex(),
        pageCount = { steps.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(recap?.completedSteps) {
        val target = recap?.firstIncompleteStep() ?: return@LaunchedEffect
        val index = steps.indexOf(target)
        if (index >= 0 && index != pagerState.currentPage) {
            pagerState.scrollToPage(index)
        }
    }

    ModalBottomSheet(onDismissRequest = {
        viewModel.clear()
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            Text(
                text = stringResource(R.string.recap_wizard_title),
                style = MaterialTheme.typography.titleLarge
            )
            val progress = (recap?.completedSteps?.size ?: 0).toFloat() / steps.size
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            HorizontalPager(state = pagerState) { page ->
                val step = steps[page]
                RecapPlaceholderStep(
                    step = step,
                    onContinue = {
                        viewModel.completeStep(step)
                        if (page < steps.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(page + 1) }
                        } else {
                            viewModel.clear()
                            onDismiss()
                        }
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        val step = steps[pagerState.currentPage]
                        viewModel.completeStep(step)
                        if (pagerState.currentPage < steps.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.clear()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.testTag("recap_skip")
                ) { Text(stringResource(R.string.recap_skip)) }
            }
        }
    }
}

@Composable
private fun RecapPlaceholderStep(step: RecapStep, onContinue: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        Text(
            text = stringResource(step.titleRes()),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.recap_step_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onContinue) {
            Text(stringResource(R.string.recap_continue))
        }
    }
}

private fun RecapStep.titleRes(): Int = when (this) {
    RecapStep.CYCLE -> R.string.recap_step_cycle
    RecapStep.PAIN -> R.string.recap_step_pain
    RecapStep.PSY -> R.string.recap_step_psy
    RecapStep.GENERAL -> R.string.recap_step_general
    RecapStep.BOWEL -> R.string.recap_step_bowel
}
