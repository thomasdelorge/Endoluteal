package fr.luteal.app.navigation.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.R
import fr.luteal.core.data.repository.QuestionnaireResultRepository
import fr.luteal.core.model.recap.InspiredMonthlyQuestionnaire
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MonthlyQuestionnaireViewModel @Inject constructor(
    private val repository: QuestionnaireResultRepository
) : ViewModel() {
    private val _period = MutableStateFlow(YearMonth.now())
    val period: StateFlow<YearMonth> = _period.asStateFlow()

    private val _answers = MutableStateFlow(List(InspiredMonthlyQuestionnaire.QUESTION_COUNT) { InspiredMonthlyQuestionnaire.ANSWER_MIN })
    val answers: StateFlow<List<Int>> = _answers.asStateFlow()

    fun openPeriod(period: YearMonth) {
        _period.value = period
        viewModelScope.launch {
            val existing = repository.getForPeriod(period)
            _answers.value = existing?.answers
                ?: List(InspiredMonthlyQuestionnaire.QUESTION_COUNT) { InspiredMonthlyQuestionnaire.ANSWER_MIN }
        }
    }

    fun setAnswer(index: Int, value: Int) {
        _answers.value = _answers.value.toMutableList().also { it[index] = value }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.save(_period.value, _answers.value)
            onDone()
        }
    }

    fun questionRes(index: Int): Int = when (index) {
        0 -> R.string.monthly_q1
        1 -> R.string.monthly_q2
        2 -> R.string.monthly_q3
        3 -> R.string.monthly_q4
        else -> R.string.monthly_q5
    }
}
