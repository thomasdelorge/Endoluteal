package fr.luteal.app.navigation.settings.meds

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.repository.MedicationRepository
import fr.luteal.core.model.meds.InteractionRule
import fr.luteal.core.model.meds.Medication
import fr.luteal.core.model.meds.MedicationCatalog
import fr.luteal.core.model.meds.MedicationSeed
import fr.luteal.core.model.meds.Substance
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationCatalogViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val catalog: StateFlow<MedicationCatalog> = medicationRepository.observeCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicationCatalog(emptyList(), emptyList(), emptyList()))

    val seedOffered: StateFlow<Boolean> = userPreferencesDataStore.userPreferencesFlow
        .map { it.medicationsSeedOffered }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val painFollowupDelayMinutes: StateFlow<Int> = userPreferencesDataStore.userPreferencesFlow
        .map { it.painFollowupDelayMinutes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 45)

    fun acceptSeed() {
        viewModelScope.launch {
            val raw = context.assets.open("medications_seed.json").bufferedReader().use { it.readText() }
            medicationRepository.applySeed(MedicationSeed.parse(raw))
            userPreferencesDataStore.setMedicationsSeedOffered(true)
        }
    }

    fun dismissSeed() {
        viewModelScope.launch { userPreferencesDataStore.setMedicationsSeedOffered(true) }
    }

    fun saveMedication(medication: Medication) {
        viewModelScope.launch { medicationRepository.upsertMedication(medication) }
    }

    fun saveSubstance(substance: Substance) {
        viewModelScope.launch { medicationRepository.upsertSubstance(substance) }
    }

    fun saveRule(rule: InteractionRule) {
        viewModelScope.launch { medicationRepository.upsertRule(rule) }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch { medicationRepository.deleteRule(id) }
    }

    fun setPainFollowupDelayMinutes(minutes: Int) {
        viewModelScope.launch { userPreferencesDataStore.setPainFollowupDelayMinutes(minutes) }
    }
}
