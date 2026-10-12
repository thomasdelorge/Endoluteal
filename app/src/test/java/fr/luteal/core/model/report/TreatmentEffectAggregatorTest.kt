package fr.luteal.core.model.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreatmentEffectAggregatorTest {
    @Test
    fun ignoresUseWithoutFollowUp() {
        val summary = TreatmentEffectAggregator.summarize(
            listOf(
                TreatmentUse(
                    kind = TreatmentKind.MEDICATION,
                    key = "med-1",
                    name = "Ibuprofène",
                    beforeMarks = listOf(6)
                )
            )
        )
        assertTrue(summary.isEmpty)
    }

    @Test
    fun averagesTwoIntakesOfTheSameMedication() {
        val summary = TreatmentEffectAggregator.summarize(
            listOf(
                TreatmentUse(
                    kind = TreatmentKind.MEDICATION,
                    key = "med-1",
                    name = "Ibuprofène",
                    beforeMarks = listOf(6),
                    afterMarks = listOf(2)
                ),
                TreatmentUse(
                    kind = TreatmentKind.MEDICATION,
                    key = "med-1",
                    name = "Ibuprofène",
                    beforeMarks = listOf(4),
                    afterMarks = listOf(4)
                )
            )
        )
        val row = summary.medications.single()
        assertEquals(2, row.pairedCount)
        assertEquals(2.0, row.meanDrop, 0.001)
        assertEquals(1, row.improved)
        assertEquals(1, row.unchanged)
        assertEquals(0, row.worse)
    }

    @Test
    fun phaseGroupSkipsDaysWithoutAPhase() {
        val summary = TreatmentEffectAggregator.summarize(
            listOf(
                TreatmentUse(
                    kind = TreatmentKind.CARE,
                    key = "heat",
                    name = "Bouillotte",
                    painBefore = 5,
                    afterMarks = listOf(3),
                    phase = "LUTEAL"
                ),
                TreatmentUse(
                    kind = TreatmentKind.CARE,
                    key = "heat",
                    name = "Bouillotte",
                    painBefore = 4,
                    afterMarks = listOf(4),
                    phase = null,
                    sleep = 4,
                    stress = 1
                )
            )
        )
        assertEquals(1, summary.byPhase.single().pairedCount)
        assertEquals("LUTEAL", summary.byPhase.single().phase)
        assertEquals(2.0, summary.byPhase.single().meanDrop, 0.001)
        assertEquals(LevelBand.HIGH, summary.bySleep.single().band)
        assertEquals(LevelBand.LOW, summary.byStress.single().band)
    }
}
