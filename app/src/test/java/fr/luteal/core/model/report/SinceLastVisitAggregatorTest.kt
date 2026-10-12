package fr.luteal.core.model.report

import fr.luteal.core.model.professional.Specialty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class SinceLastVisitAggregatorTest {
    private val zone = ZoneOffset.UTC
    private val monday = Instant.parse("2026-06-01T10:00:00Z")
    private val laterSameWeek = Instant.parse("2026-06-03T10:00:00Z")
    private val outside = Instant.parse("2026-05-01T10:00:00Z")

    @Test
    fun summarizesFixedWindow() {
        val summary = SinceLastVisitAggregator.summarize(
            fromInclusive = Instant.parse("2026-06-01T00:00:00Z"),
            toInclusive = Instant.parse("2026-06-30T00:00:00Z"),
            zone = zone,
            pain = listOf(
                TimelinePainSample(monday, "pelvis", 2),
                TimelinePainSample(laterSameWeek, "pelvis", 4),
                TimelinePainSample(laterSameWeek, "back", 1),
                TimelinePainSample(outside, "pelvis", 10)
            ),
            intakes = listOf(
                TimelineIntakeSample(monday, "med-1", "Ibuprofène"),
                TimelineIntakeSample(laterSameWeek, "med-1", "Ibuprofène"),
                TimelineIntakeSample(outside, "med-2", "Autre")
            ),
            hospitalizations = listOf(monday, outside),
            careSessions = listOf(laterSameWeek, outside)
        )
        assertEquals(1, summary.weeklyPain.size)
        assertEquals(7.0 / 3.0, summary.weeklyPain.single().average, 0.001)
        assertEquals(4, summary.weeklyPain.single().max)
        assertEquals(listOf(ZonePeak("back", 1), ZonePeak("pelvis", 4)), summary.zonePeaks)
        assertEquals(1, summary.intakes.size)
        assertEquals(2, summary.intakes.single().count)
        assertEquals(1, summary.hospitalizationCount)
        assertEquals(1, summary.careSessionCount)
    }
}

class TimelineFilterTest {
    @Test
    fun professionalFilterKeepsOnlyThatProfessionalsAppointments() {
        val visits = listOf(
            visit("a", TimelineVisitKind.APPOINTMENT, "pro-1"),
            visit("b", TimelineVisitKind.APPOINTMENT, "pro-2"),
            visit("h", TimelineVisitKind.HOSPITALIZATION, null)
        )
        val filtered = TimelineFilter.apply(visits, professionalId = "pro-1", specialty = null, fromInclusive = null, toInclusive = null)
        assertEquals(listOf("a"), filtered.map { it.eventId })
    }

    @Test
    fun specialtyFilter() {
        val visits = listOf(
            visit("a", TimelineVisitKind.APPOINTMENT, "pro-1", Specialty.GYNECOLOGIST),
            visit("b", TimelineVisitKind.APPOINTMENT, "pro-2", Specialty.RADIOLOGIST)
        )
        val filtered = TimelineFilter.apply(
            visits,
            professionalId = null,
            specialty = Specialty.GYNECOLOGIST,
            fromInclusive = null,
            toInclusive = null
        )
        assertEquals(listOf("a"), filtered.map { it.eventId })
        assertTrue(filtered.all { it.specialty == Specialty.GYNECOLOGIST })
    }

    @Test
    fun pdfContentKeepsOnlyTheSelectedProfessional() {
        val visits = listOf(
            visit("a", TimelineVisitKind.APPOINTMENT, "pro-1"),
            visit("b", TimelineVisitKind.APPOINTMENT, "pro-2"),
            visit("h", TimelineVisitKind.HOSPITALIZATION, null)
        )
        val content = buildTimelinePdfContent(
            visits = visits,
            professionalId = "pro-1",
            specialty = null,
            fromInclusive = null,
            toInclusive = null,
            summary = null
        )
        assertEquals(listOf("a"), content.visits.map { it.eventId })
    }

    private fun visit(
        id: String,
        kind: TimelineVisitKind,
        professionalId: String?,
        specialty: Specialty? = Specialty.GYNECOLOGIST
    ) = TimelineVisit(
        eventId = id,
        kind = kind,
        professionalId = professionalId,
        specialty = specialty,
        at = Instant.parse("2026-06-01T10:00:00Z"),
        title = id,
        subtitle = "",
        detail = ""
    )
}
