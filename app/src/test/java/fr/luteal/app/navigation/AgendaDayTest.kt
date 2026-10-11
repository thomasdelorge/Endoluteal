package fr.luteal.app.navigation

import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AgendaDayTest {

    @Test
    fun eventsOnDayKeepsEventsRecordedThatDay() {
        val zone = ZoneId.of("Europe/Paris")
        val day = LocalDate.parse("2026-08-01")
        val onDay = event("on-day", day, zone)
        val nextDay = event("next-day", day.plusDays(1), zone)

        assertEquals(listOf("on-day"), eventsOnDay(listOf(nextDay, onDay), day).map { it.id })
        assertEquals(mapOf(day to 1, day.plusDays(1) to 1), eventCountsByDay(listOf(onDay, nextDay)))
    }

    private fun event(id: String, day: LocalDate, zone: ZoneId) = JournalEvent(
        id = id,
        type = JournalEventType.SYMPTOM,
        startAt = day.atTime(8, 30).atZone(zone).toInstant(),
        endAt = null,
        zoneId = zone.id,
        notes = "",
        linkedEventId = null,
        payload = EventPayload.Symptom,
        marks = emptyList(),
        createdAt = day.atStartOfDay(zone).toInstant(),
        updatedAt = day.atStartOfDay(zone).toInstant()
    )
}
