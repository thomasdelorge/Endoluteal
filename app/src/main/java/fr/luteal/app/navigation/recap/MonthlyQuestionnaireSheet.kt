package fr.luteal.app.navigation.recap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.FullScreenEntry
import fr.luteal.core.designsystem.component.ScreenBack
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.recap.InspiredMonthlyQuestionnaire
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyQuestionnaireSheet(
    period: YearMonth,
    viewModel: MonthlyQuestionnaireViewModel,
    onDismiss: () -> Unit
) {
    LaunchedEffect(period) { viewModel.openPeriod(period) }
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { InspiredMonthlyQuestionnaire.QUESTION_COUNT })

    FullScreenEntry {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            ScreenBack(onClick = onDismiss)
            Text(
                text = stringResource(R.string.monthly_questionnaire_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.monthly_questionnaire_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                    Text(stringResource(viewModel.questionRes(page)), style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                        (InspiredMonthlyQuestionnaire.ANSWER_MIN..InspiredMonthlyQuestionnaire.ANSWER_MAX).forEach { value ->
                            FilterChip(
                                selected = answers[page] == value,
                                onClick = { viewModel.setAnswer(page, value) },
                                label = { Text(value.toString()) }
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick = { viewModel.save(onDismiss) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.recap_continue)) }
        }
    }
}
