package fr.luteal.app.navigation.recap

import fr.luteal.core.model.CurrentCyclePhase
import fr.luteal.core.model.CyclePhase

data class RecapCycleUiState(
    val estimatedPhase: CyclePhase?,
    val phase: CurrentCyclePhase,
    val isEstimate: Boolean
)
