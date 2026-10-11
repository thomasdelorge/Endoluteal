package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.ProfessionalEntity
import fr.luteal.core.data.local.ProfessionalDao
import fr.luteal.core.model.professional.Professional
import fr.luteal.core.model.professional.Specialty
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProfessionalRepository {
    fun observeAll(): Flow<List<Professional>>
    suspend fun allOnce(): List<Professional>
    suspend fun upsert(professional: Professional)
}

@Singleton
class ProfessionalRepositoryImpl @Inject constructor(
    private val professionalDao: ProfessionalDao
) : ProfessionalRepository {
    override fun observeAll(): Flow<List<Professional>> =
        professionalDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun allOnce(): List<Professional> =
        professionalDao.allOnce().map { it.toModel() }

    override suspend fun upsert(professional: Professional) {
        require(professional.name.isNotBlank()) { "Professional name is required" }
        professionalDao.upsert(professional.toEntity())
    }

    private fun ProfessionalEntity.toModel(): Professional = Professional(
        id = id,
        name = name,
        specialty = Specialty.fromStored(specialty),
        customSpecialty = customSpecialty,
        place = place,
        phone = phone,
        notes = notes
    )

    private fun Professional.toEntity(): ProfessionalEntity = ProfessionalEntity(
        id = id,
        name = name,
        specialty = specialty.name,
        customSpecialty = customSpecialty,
        place = place,
        phone = phone,
        notes = notes
    )
}
