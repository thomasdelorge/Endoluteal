package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.CareTypeEntity
import fr.luteal.core.data.local.CareTypeDao
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

interface CareTypeRepository {
    fun observeActive(): Flow<List<CareType>>
    fun observeAll(): Flow<List<CareType>>
    suspend fun allOnce(): List<CareType>
    suspend fun upsert(type: CareType)
    suspend fun applySeed(types: List<CareType>)
}

@Singleton
class CareTypeRepositoryImpl @Inject constructor(
    private val careTypeDao: CareTypeDao
) : CareTypeRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeActive(): Flow<List<CareType>> =
        careTypeDao.observeActive().map { list -> list.map { it.toModel() } }

    override fun observeAll(): Flow<List<CareType>> =
        careTypeDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun allOnce(): List<CareType> = careTypeDao.allOnce().map { it.toModel() }

    override suspend fun upsert(type: CareType) {
        require(type.name.isNotBlank()) { "Care type name is required" }
        careTypeDao.upsert(type.toEntity())
    }

    override suspend fun applySeed(types: List<CareType>) {
        types.forEach { upsert(it) }
    }

    private fun CareTypeEntity.toModel(): CareType = CareType(
        id = id,
        name = name,
        programs = programsJson.toPrograms(),
        archived = archived
    )

    private fun CareType.toEntity(): CareTypeEntity = CareTypeEntity(
        id = id,
        name = name,
        programsJson = programs.toJson(),
        archived = archived
    )

    companion object {
        fun List<CareProgram>.toJson(): String {
            val array = JSONArray()
            forEach { program ->
                array.put(
                    JSONObject()
                        .put("name", program.name)
                        .put("defaultDurationMinutes", program.defaultDurationMinutes)
                )
            }
            return array.toString()
        }

        fun String.toPrograms(): List<CareProgram> = runCatching {
            val array = JSONArray(this)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                CareProgram(
                    name = item.getString("name"),
                    defaultDurationMinutes = item.getInt("defaultDurationMinutes")
                )
            }
        }.getOrDefault(emptyList())
    }
}
