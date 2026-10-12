package fr.luteal.core.calendar

import fr.luteal.core.model.professional.ReportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarImportEngineTest {
    @Test
    fun calendarEventKeyFormat() {
        assertEquals("42:1000", CalendarImportEngine.calendarEventKey(42, 1000))
    }

    @Test
    fun suggestsProfessionalFromSameTitle() {
        val id = CalendarImportEngine.suggestProfessionalId(
            title = "Gynéco",
            location = "",
            history = listOf(
                ProfessionalSuggestionHistory("pro-1", "Gynéco", "Hôpital", 1000L),
                ProfessionalSuggestionHistory("pro-2", "Autre", "", 2000L)
            )
        )
        assertEquals("pro-1", id)
    }

    @Test
    fun cancelsMissingImportedWithoutReport() {
        val plan = CalendarImportEngine.planImport(
            instances = emptyList(),
            existingImported = listOf(
                ExistingImportedAppointment(
                    eventId = "e1",
                    calendarEventKey = "1:100",
                    professionalId = "",
                    location = "",
                    report = "",
                    reportStatus = ReportStatus.PENDING,
                    title = "RDV",
                    startAtEpochMillis = 100,
                    endAtEpochMillis = 200
                )
            ),
            history = emptyList()
        )
        assertEquals(listOf("e1"), plan.cancelEventIds)
    }

    @Test
    fun doesNotCancelWhenReportPresent() {
        val plan = CalendarImportEngine.planImport(
            instances = emptyList(),
            existingImported = listOf(
                ExistingImportedAppointment(
                    eventId = "e1",
                    calendarEventKey = "1:100",
                    professionalId = "pro",
                    location = "",
                    report = "Notes",
                    reportStatus = ReportStatus.DONE,
                    title = "RDV",
                    startAtEpochMillis = 100,
                    endAtEpochMillis = 200
                )
            ),
            history = emptyList()
        )
        assertTrue(plan.cancelEventIds.isEmpty())
    }
}
