package fr.luteal.core.model.meds

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreatmentPlanScheduleTest {

    @Test
    fun twoPlansAtTheSameTimeBecomeOneGroup() {
        val today = LocalDate.parse("2026-08-01")
        val plans = listOf(
            plan("a", "med-a", LocalTime.of(8, 0), today, null),
            plan("b", "med-b", LocalTime.of(8, 0), today, null)
        )
        val grouped = nextGroupedIntake(plans, LocalDateTime.parse("2026-08-01T07:00:00"))
        assertEquals(LocalDateTime.parse("2026-08-01T08:00:00"), grouped?.at)
        assertEquals(listOf("med-a", "med-b"), grouped?.doses?.map { it.medicationId })
    }

    @Test
    fun nothingOnOrAfterEndDate() {
        val plan = plan(
            "a",
            "med-a",
            LocalTime.of(8, 0),
            LocalDate.parse("2026-08-01"),
            LocalDate.parse("2026-08-01")
        )
        assertNull(nextGroupedIntake(listOf(plan), LocalDateTime.parse("2026-08-01T07:00:00")))
    }

    @Test
    fun hormonalFlagCoversTheActiveRangeOnly() {
        val plan = plan(
            "h",
            "med-h",
            LocalTime.of(8, 0),
            LocalDate.parse("2026-08-01"),
            LocalDate.parse("2026-08-10")
        ).copy(continuousHormonal = true)
        assertEquals(true, continuousHormonalActive(listOf(plan), LocalDate.parse("2026-08-05")))
        assertEquals(false, continuousHormonalActive(listOf(plan), LocalDate.parse("2026-08-10")))
        assertEquals(false, continuousHormonalActive(listOf(plan.copy(active = false)), LocalDate.parse("2026-08-05")))
    }

    private fun plan(
        id: String,
        medicationId: String,
        time: LocalTime,
        start: LocalDate,
        end: LocalDate?
    ) = TreatmentPlan(id, medicationId, 1.0, listOf(time), start, end, active = true, continuousHormonal = false)
}
