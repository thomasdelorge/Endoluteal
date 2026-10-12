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

    fun setPainFollowupDelayMinutes(minutes: Int) {
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
        viewModelScope.launch { insertProgram(typeId, program) }
    }

    fun renameProgram(typeId: String, previousName: String, program: CareProgram) {
        viewModelScope.launch {
            val current = careTypeRepository.allOnce().find { it.id == typeId } ?: return@launch
            val next = current.programs.map { existing ->
                if (existing.name == previousName) program else existing
            }
            if (next == current.programs) return@launch
            if (next.count { it.name == program.name } > 1) return@launch
            careTypeRepository.upsert(current.copy(programs = next))
        }
    }

    fun removeProgram(typeId: String, name: String) {
        viewModelScope.launch {
            val current = careTypeRepository.allOnce().find { it.id == typeId } ?: return@launch
            careTypeRepository.upsert(
                current.copy(programs = current.programs.filterNot { it.name == name })
            )
        }
    }

    init {
        viewModelScope.launch {
            if (careTypeRepository.allOnce().none { it.id == CareTypeSeed.TENS_ID }) {
                careTypeRepository.upsert(CareTypeSeed.defaults().first { it.id == CareTypeSeed.TENS_ID })
            }
        }
    }

    private suspend fun insertProgram(typeId: String, program: CareProgram) {
        val current = careTypeRepository.allOnce().find { it.id == typeId } ?: return
        if (program.name.isBlank() || current.programs.any { it.name == program.name }) return
        careTypeRepository.upsert(current.copy(programs = current.programs + program))
    }
}
