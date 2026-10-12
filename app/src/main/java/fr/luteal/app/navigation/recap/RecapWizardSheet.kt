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
import fr.luteal.app.navigation.recap.steps.BowelStep
import fr.luteal.app.navigation.recap.steps.CycleStep
import fr.luteal.app.navigation.recap.steps.GeneralStep
import fr.luteal.app.navigation.recap.steps.PainStep
import fr.luteal.app.navigation.recap.steps.PsyStep
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.recap.RecapBowelPayload
import fr.luteal.core.model.recap.RecapCyclePayload
import fr.luteal.core.model.recap.RecapGeneralPayload
import fr.luteal.core.model.recap.RecapPainPayload
import fr.luteal.core.model.recap.RecapPsyPayload
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
    val cycleUi by viewModel.cycleUi.collectAsStateWithLifecycle()
    val dayEvents by viewModel.dayEventLines.collectAsStateWithLifecycle()
    val hideDyspareunia by viewModel.hideDyspareunia.collectAsStateWithLifecycle()
    val hideLibido by viewModel.hideLibido.collectAsStateWithLifecycle()
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

    fun advanceFrom(page: Int) {
        if (page < steps.lastIndex) {
            scope.launch { pagerState.animateScrollToPage(page + 1) }
        } else {
            viewModel.clear()
            onDismiss()
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
            val currentRecap = recap
            HorizontalPager(state = pagerState) { page ->
                val step = steps[page]
                when (step) {
                    RecapStep.CYCLE -> CycleStep(
                        ui = cycleUi,
                        initial = currentRecap?.cycle ?: RecapCyclePayload(),
                        onContinue = { payload ->
                            viewModel.finishCycleStep(payload) { advanceFrom(page) }
                        }
                    )
                    RecapStep.PAIN -> PainStep(
                        dayEventLines = dayEvents,
                        hideDyspareunia = hideDyspareunia,
                        initial = currentRecap?.pain ?: RecapPainPayload(),
                        onContinue = { payload ->
                            viewModel.finishPainStep(payload) { advanceFrom(page) }
                        }
                    )
                    RecapStep.PSY -> PsyStep(
                        hideLibido = hideLibido,
                        initial = currentRecap?.psy ?: RecapPsyPayload(),
                        onContinue = { payload ->
                            viewModel.finishPsyStep(payload) { advanceFrom(page) }
                        }
                    )
                    RecapStep.GENERAL -> GeneralStep(
                        initial = currentRecap?.general ?: RecapGeneralPayload(),
                        onContinue = { payload ->
                            viewModel.finishGeneralStep(payload) { advanceFrom(page) }
                        }
                    )
                    RecapStep.BOWEL -> BowelStep(
                        initial = currentRecap?.bowel ?: RecapBowelPayload(),
                        onContinue = { payload ->
                            viewModel.finishBowelStep(payload) { advanceFrom(page) }
                        }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        val step = steps[pagerState.currentPage]
                        viewModel.completeStep(step)
                        advanceFrom(pagerState.currentPage)
                    },
                    modifier = Modifier.testTag("recap_skip")
                ) { Text(stringResource(R.string.recap_skip)) }
            }
        }
    }
}
