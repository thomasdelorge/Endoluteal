package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.QuestionnaireResultEntity
import fr.luteal.core.data.local.QuestionnaireResultDao
import fr.luteal.core.model.recap.InspiredMonthlyQuestionnaire
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class QuestionnaireResult(
    val id: String,
    val period: YearMonth,
    val answers: List<Int>,
    val score: Int,
    val completedAt: Instant
)

interface QuestionnaireResultRepository {
    suspend fun allOnce(): List<QuestionnaireResult>
    suspend fun getForPeriod(period: YearMonth): QuestionnaireResult?
    suspend fun save(period: YearMonth, answers: List<Int>): QuestionnaireResult
}

@Singleton
class QuestionnaireResultRepositoryImpl @Inject constructor(
    private val dao: QuestionnaireResultDao,
    private val clock: Clock
) : QuestionnaireResultRepository {
    private val json = Json { encodeDefaults = true }

    override suspend fun allOnce(): List<QuestionnaireResult> =
        dao.allOnce().map { it.toModel() }

    override suspend fun getForPeriod(period: YearMonth): QuestionnaireResult? =
        dao.getForPeriod(period.toString())?.toModel()

    override suspend fun save(period: YearMonth, answers: List<Int>): QuestionnaireResult {
        val score = InspiredMonthlyQuestionnaire.score(answers)
        val now = clock.instant()
        val entity = QuestionnaireResultEntity(
            id = UUID.randomUUID().toString(),
            period = period.toString(),
            answersJson = json.encodeToString(answers),
            score = score,
            completedAtEpochMillis = now.toEpochMilli()
        )
        dao.upsert(entity)
        return entity.toModel()
    }

    private fun QuestionnaireResultEntity.toModel(): QuestionnaireResult {
        val answers = json.decodeFromString<List<Int>>(answersJson)
        return QuestionnaireResult(
            id = id,
            period = YearMonth.parse(period),
            answers = answers,
            score = score,
            completedAt = Instant.ofEpochMilli(completedAtEpochMillis)
        )
    }
}
