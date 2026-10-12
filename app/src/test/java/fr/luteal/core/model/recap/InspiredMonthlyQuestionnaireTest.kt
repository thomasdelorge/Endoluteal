package fr.luteal.core.model.recap

import org.junit.Assert.assertEquals
import org.junit.Test

class InspiredMonthlyQuestionnaireTest {

    @Test
    fun scoreIsNormalizedToHundred() {
        val worst = List(InspiredMonthlyQuestionnaire.QUESTION_COUNT) { 4 }
        val best = List(InspiredMonthlyQuestionnaire.QUESTION_COUNT) { 0 }
        assertEquals(100, InspiredMonthlyQuestionnaire.score(worst))
        assertEquals(0, InspiredMonthlyQuestionnaire.score(best))
    }
}
