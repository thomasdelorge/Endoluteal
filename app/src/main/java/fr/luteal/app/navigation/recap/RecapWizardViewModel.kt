package fr.luteal.app.navigation.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.DailyRecapRepository
import fr.luteal.core.model.recap.DailyRecap
import fr.luteal.core.model.recap.RecapStep
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecapWizardViewModel @Inject constructor(
    private val dailyRecapRepository: DailyRecapRepository,
    private val clock: Clock
) : ViewModel() {
    private val _recapDate = MutableStateFlow<LocalDate?>(null)
    val recapDate: StateFlow<LocalDate?> = _recapDate.asStateFlow()

    private val _recap = MutableStateFlow<DailyRecap?>(null)
    val recap: StateFlow<DailyRecap?> = _recap.asStateFlow()

    fun open(date: LocalDate) {
        if (_recapDate.value == date) return
        _recapDate.value = date
        viewModelScope.launch {
            val existing = dailyRecapRepository.get(date)
            _recap.value = existing ?: DailyRecap.empty(date, clock.instant())
        }
    }

    fun initialPageIndex(): Int {
        val recap = _recap.value ?: return 0
        return RecapStep.ordered.indexOf(recap.firstIncompleteStep()).coerceAtLeast(0)
    }

    fun completeStep(step: RecapStep) {
        val date = _recapDate.value ?: return
        viewModelScope.launch {
            dailyRecapRepository.markStepComplete(date, step)
            _recap.value = dailyRecapRepository.get(date)
        }
    }

    fun clear() {
        _recapDate.value = null
        _recap.value = null
    }
}
