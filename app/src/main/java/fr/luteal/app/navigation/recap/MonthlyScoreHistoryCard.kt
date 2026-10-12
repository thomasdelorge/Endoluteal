package fr.luteal.app.navigation.recap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.R
import fr.luteal.core.data.repository.QuestionnaireResultRepository
import fr.luteal.core.designsystem.component.LutealCard
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonthlyScoreRow(
    val periodLabel: String,
    val score: Int
)

@HiltViewModel
class MonthlyScoreHistoryViewModel @Inject constructor(
    repository: QuestionnaireResultRepository
) : ViewModel() {
    private val _rows = MutableStateFlow<List<MonthlyScoreRow>>(emptyList())
    val rows: StateFlow<List<MonthlyScoreRow>> = _rows.asStateFlow()

    init {
        viewModelScope.launch {
            _rows.value = repository.allOnce().map { result ->
                MonthlyScoreRow(
                    periodLabel = result.period.format(PERIOD_FORMAT),
                    score = result.score
                )
            }
        }
    }

    private companion object {
        val PERIOD_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)
    }
}

@Composable
fun MonthlyScoreHistoryCard(
    viewModel: MonthlyScoreHistoryViewModel = hiltViewModel()
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    LutealCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(R.string.monthly_score_history_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.monthly_score_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                rows.forEach { row ->
                    Text(
                        text = stringResource(
                            R.string.monthly_score_history_row,
                            row.periodLabel,
                            row.score
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
