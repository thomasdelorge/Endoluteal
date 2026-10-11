package fr.luteal.app.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import fr.luteal.app.LutealUiState
import fr.luteal.app.R
import fr.luteal.core.designsystem.theme.LutealTheme
import fr.luteal.core.model.DailyEntry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class LutealScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Expected text is read from the same resources the screen renders, so
     * these assertions hold whichever of the shipped languages the device is
     * set to. Hard-coded French only worked while French was the sole possible
     * resolution.
     */
    private fun string(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    @Test
    fun emptyJournalUsesTeachingEmptyStateInsteadOfEmptyDayRows() {
        val today = LocalDate.parse("2026-07-20")

        composeRule.setContent {
            LutealTheme(darkTheme = false) {
                JournalScreen(
                    state = LutealUiState(today = today),
                    onSelectDate = {},
                    initialViewMode = JournalViewMode.TIMELINE
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.journal_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.journal_no_observation)).assertDoesNotExist()
    }

    @Test
    fun journalShowsOnlyRecordedHistoryAndOpensSelectedDate() {
        val today = LocalDate.parse("2026-07-20")
        val recordedDate = today.minusDays(3)
        var selectedDate: LocalDate? = null

        composeRule.setContent {
            LutealTheme(darkTheme = false) {
                JournalScreen(
                    state = LutealUiState(
                        today = today,
                        entries = listOf(
                            DailyEntry(date = today.minusDays(1)),
                            DailyEntry(date = recordedDate, painLevel = 3)
                        )
                    ),
                    onSelectDate = { selectedDate = it },
                    initialViewMode = JournalViewMode.TIMELINE
                )
            }
        }

        // Journal levels are drawn as filled segments; the label and value
        // travel together in the accessible name.
        composeRule.onNodeWithContentDescription(
            string(R.string.level_a11y, string(R.string.level_label_pain), 3)
        ).performClick()
        assertEquals(recordedDate, selectedDate)
    }

    @Test
    fun calendarViewShowsMonthGridAndInspectsSelectedDay() {
        val today = LocalDate.parse("2026-08-13")
        var openedDate: LocalDate? = null

        composeRule.setContent {
            LutealTheme(darkTheme = false) {
                JournalScreen(
                    state = LutealUiState(
                        today = today,
                        entries = listOf(
                            DailyEntry(date = today, painLevel = 2)
                        )
                    ),
                    onSelectDate = { openedDate = it },
                    initialViewMode = JournalViewMode.CALENDAR
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.calendar_view_calendar)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.calendar_legend_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_edit_today)).performClick()
        assertEquals(today, openedDate)
    }

    // DuoScreen now uses hiltViewModel() internally and requires a Hilt
    // integration test setup; the former local-state toggle test is removed.
}
