package fr.luteal.app.navigation.settings.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.repository.CareTypeRepository
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType
import fr.luteal.core.model.care.CareTypeSeed
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CareCatalogViewModel @Inject constructor(
    private val careTypeRepository: CareTypeRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {
    val types: StateFlow<List<CareType>> = careTypeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val seedOffered: StateFlow<Boolean> = userPreferencesDataStore.userPreferencesFlow
        .map { it.careTypesSeedOffered }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun acceptSeed() {
        viewModelScope.launch {
            careTypeRepository.applySeed(CareTypeSeed.defaults())
            userPreferencesDataStore.setCareTypesSeedOffered(true)
        }
    }

    fun dismissSeed() {
        viewModelScope.launch { userPreferencesDataStore.setCareTypesSeedOffered(true) }
    }

    fun save(type: CareType) {
        viewModelScope.launch { careTypeRepository.upsert(type) }
    }

    fun addCustomType(name: String) {
        viewModelScope.launch {
            careTypeRepository.upsert(
                CareType(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    programs = emptyList(),
                    archived = false
                )
            )
        }
    }

    fun addProgram(typeId: String, program: CareProgram) {
        viewModelScope.launch {
            val current = careTypeRepository.allOnce().find { it.id == typeId } ?: return@launch
            if (current.programs.any { it.name == program.name }) return@launch
            careTypeRepository.upsert(current.copy(programs = current.programs + program))
        }
    }
}
