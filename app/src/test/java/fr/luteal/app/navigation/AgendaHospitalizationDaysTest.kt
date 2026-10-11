package fr.luteal.app.navigation

import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaHospitalizationDaysTest {

    private val zone = ZoneId.of("Europe/Paris")

    @Test
    fun hospitalizationAppearsOnEachDayUntilDischarge() {
        val event = hospitalization(
            start = LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant(),
            end = LocalDate.of(2026, 1, 3).atTime(18, 0).atZone(zone).toInstant()
        )
        val events = listOf(event)
        assertEquals(1, eventsOnDay(events, LocalDate.of(2026, 1, 1)).size)
        assertEquals(1, eventsOnDay(events, LocalDate.of(2026, 1, 3)).size)
        assertTrue(eventsOnDay(events, LocalDate.of(2026, 1, 2)).isNotEmpty())
        assertTrue(eventsOnDay(events, LocalDate.of(2026, 1, 4)).isEmpty())
        val counts = eventCountsByDay(events)
        assertEquals(1, counts[LocalDate.of(2026, 1, 1)])
        assertEquals(1, counts[LocalDate.of(2026, 1, 3)])
        assertFalse(counts.containsKey(LocalDate.of(2026, 1, 4)))
    }

    @Test
    fun ongoingHospitalizationOnlyOnAdmissionDay() {
        val event = hospitalization(
            start = LocalDate.of(2026, 2, 10).atStartOfDay(zone).toInstant(),
            end = null
        )
        val events = listOf(event)
        assertEquals(1, eventsOnDay(events, LocalDate.of(2026, 2, 10)).size)
        assertTrue(eventsOnDay(events, LocalDate.of(2026, 2, 11)).isEmpty())
    }

    private fun hospitalization(start: Instant, end: Instant?): JournalEvent {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return JournalEvent(
            id = "hosp-1",
            type = JournalEventType.HOSPITALIZATION,
            startAt = start,
            endAt = end,
            zoneId = zone.id,
            notes = "",
            linkedEventId = null,
            payload = EventPayload.Hospitalization(place = "CHU"),
            marks = emptyList(),
            createdAt = now,
            updatedAt = now
        )
    }
}
