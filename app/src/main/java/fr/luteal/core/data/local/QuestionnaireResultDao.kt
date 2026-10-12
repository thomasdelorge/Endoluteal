package fr.luteal.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.luteal.core.data.entity.QuestionnaireResultEntity

@Dao
interface QuestionnaireResultDao {
    @Query("SELECT * FROM questionnaire_results ORDER BY period DESC")
    suspend fun allOnce(): List<QuestionnaireResultEntity>

    @Query("SELECT * FROM questionnaire_results WHERE period = :period LIMIT 1")
    suspend fun getForPeriod(period: String): QuestionnaireResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionnaireResultEntity)
}
