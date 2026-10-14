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
    fun doesNotCancelWhenCalendarEventDisappears() {
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
        assertTrue(plan.cancelEventIds.isEmpty())
        assertTrue(plan.upserts.isEmpty())
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

    @Test
    fun keepsAssignedProfessionalOnReimport() {
        val plan = CalendarImportEngine.planImport(
            instances = listOf(
                ImportedCalendarInstance(
                    calendarEventKey = "1:100",
                    title = "Gynéco",
                    description = "",
                    location = "Cabinet",
                    startAtEpochMillis = 100,
                    endAtEpochMillis = 200
                )
            ),
            existingImported = listOf(
                ExistingImportedAppointment(
                    eventId = "e1",
                    calendarEventKey = "1:100",
                    professionalId = "pro-9",
                    location = "Ancien lieu",
                    report = "",
                    reportStatus = ReportStatus.PENDING,
                    title = "Gynéco",
                    startAtEpochMillis = 100,
                    endAtEpochMillis = 200
                )
            ),
            history = emptyList()
        )
        val upsert = plan.upserts.single()
        assertEquals("e1", upsert.existingEventId)
        assertEquals("pro-9", upsert.professionalId)
        assertEquals("Cabinet", upsert.location)
    }

    @Test
    fun updatesTitleAndKeepsOriginalTimeWhenBeginChanges() {
        val plan = CalendarImportEngine.planImport(
            instances = listOf(
                ImportedCalendarInstance(
                    calendarEventKey = "7:999",
                    title = "Nouveau titre",
                    description = "",
                    location = "Nouveau lieu",
                    startAtEpochMillis = 999,
                    endAtEpochMillis = 1200
                )
            ),
            existingImported = listOf(
                ExistingImportedAppointment(
                    eventId = "e1",
                    calendarEventKey = "7:100",
                    professionalId = "pro-9",
                    location = "Ancien lieu",
                    report = "CR",
                    reportStatus = ReportStatus.DONE,
                    title = "Ancien titre",
                    startAtEpochMillis = 100,
                    endAtEpochMillis = 200
                )
            ),
            history = emptyList()
        )
        val upsert = plan.upserts.single()
        assertEquals("e1", upsert.existingEventId)
        assertEquals("Nouveau titre", upsert.title)
        assertEquals("Nouveau lieu", upsert.location)
        assertEquals("pro-9", upsert.professionalId)
        assertEquals(100L, upsert.startAtEpochMillis)
        assertEquals(ReportStatus.DONE, upsert.reportStatus)
        assertTrue(plan.cancelEventIds.isEmpty())
    }
}
