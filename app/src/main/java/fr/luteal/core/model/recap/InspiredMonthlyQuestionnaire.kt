package fr.luteal.core.model.recap

import kotlin.math.roundToInt

/**
 * Monthly quality-of-life questionnaire with original wording (not EHP-5).
 * Each answer is 0 (best) to 4 (worst); score 0–100 is the normalized sum.
 */
object InspiredMonthlyQuestionnaire {
    const val QUESTION_COUNT = 5
    const val ANSWER_MIN = 0
    const val ANSWER_MAX = 4

    fun score(answers: List<Int>): Int {
        if (answers.size != QUESTION_COUNT) return 0
        if (answers.any { it !in ANSWER_MIN..ANSWER_MAX }) return 0
        val sum = answers.sum()
        val max = QUESTION_COUNT * ANSWER_MAX
        return ((sum.toDouble() / max) * 100).roundToInt().coerceIn(0, 100)
    }
}
