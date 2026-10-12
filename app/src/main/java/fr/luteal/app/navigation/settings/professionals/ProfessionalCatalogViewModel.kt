package fr.luteal.app.navigation.settings.professionals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.core.data.repository.ProfessionalRepository
import fr.luteal.core.model.professional.Professional
import fr.luteal.core.model.professional.Specialty
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfessionalCatalogViewModel @Inject constructor(
    private val professionalRepository: ProfessionalRepository
) : ViewModel() {
    val professionals: StateFlow<List<Professional>> = professionalRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(professional: Professional) {
        viewModelScope.launch { professionalRepository.upsert(professional) }
    }

    fun create(name: String, specialty: Specialty, customSpecialty: String) {
        viewModelScope.launch {
            professionalRepository.upsert(
                Professional(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    specialty = specialty,
                    customSpecialty = customSpecialty.trim(),
                    place = "",
                    phone = "",
                    notes = ""
                )
            )
        }
    }

    fun update(professional: Professional) {
        viewModelScope.launch { professionalRepository.upsert(professional) }
    }

    fun delete(id: String) {
        viewModelScope.launch { professionalRepository.delete(id) }
    }
}
