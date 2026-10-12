package fr.luteal.core.calendar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarEventFilterTest {
    @Test
    fun matchesKeywordCaseInsensitive() {
        assertTrue(
            CalendarEventFilter.matches(
                title = "Gynéco",
                description = "",
                keywords = listOf("gynéco")
            )
        )
    }

    @Test
    fun matchesEmojiInTitle() {
        assertTrue(
            CalendarEventFilter.matches(
                title = "🩺 Gynéco",
                description = "",
                keywords = listOf("🩺")
            )
        )
    }

    @Test
    fun emptyKeywordsNeverMatches() {
        assertFalse(CalendarEventFilter.matches("RDV", "desc", emptyList()))
    }
}
