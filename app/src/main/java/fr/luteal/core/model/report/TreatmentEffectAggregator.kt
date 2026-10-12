package fr.luteal.core.model.report

enum class TreatmentKind {
    MEDICATION,
    CARE
}

enum class LevelBand {
    LOW,
    HIGH
}

data class TreatmentUse(
    val kind: TreatmentKind,
    val key: String,
    val name: String,
    val beforeMarks: List<Int> = emptyList(),
    val painBefore: Int? = null,
    val afterMarks: List<Int> = emptyList(),
    val phase: String? = null,
    val sleep: Int? = null,
    val stress: Int? = null
)

data class TreatmentEffectRow(
    val kind: TreatmentKind,
    val key: String,
    val name: String,
    val pairedCount: Int,
    val meanDrop: Double,
    val improved: Int,
    val unchanged: Int,
    val worse: Int
)

data class PhaseEffect(
    val phase: String,
    val pairedCount: Int,
    val meanDrop: Double
)

data class BandEffect(
    val band: LevelBand,
    val pairedCount: Int,
    val meanDrop: Double
)

data class TreatmentEffectSummary(
    val medications: List<TreatmentEffectRow>,
    val careTypes: List<TreatmentEffectRow>,
    val byPhase: List<PhaseEffect>,
    val bySleep: List<BandEffect>,
    val byStress: List<BandEffect>
) {
    val isEmpty: Boolean get() = medications.isEmpty() && careTypes.isEmpty()
}

object TreatmentEffectAggregator {
    fun summarize(uses: List<TreatmentUse>): TreatmentEffectSummary {
        val paired = uses.mapNotNull { use ->
            val before = use.beforeMarks.maxOrNull() ?: use.painBefore ?: return@mapNotNull null
            val after = use.afterMarks.maxOrNull() ?: return@mapNotNull null
            Paired(use, before - after)
        }
        return TreatmentEffectSummary(
            medications = rows(paired, TreatmentKind.MEDICATION),
            careTypes = rows(paired, TreatmentKind.CARE),
            byPhase = paired
                .filter { !it.use.phase.isNullOrBlank() }
                .groupBy { it.use.phase.orEmpty() }
                .map { (phase, items) ->
                    PhaseEffect(phase, items.size, items.map { it.drop }.average())
                }
                .sortedBy { it.phase },
            bySleep = bands(paired) { it.use.sleep },
            byStress = bands(paired) { it.use.stress }
        )
    }

    private fun rows(paired: List<Paired>, kind: TreatmentKind): List<TreatmentEffectRow> =
        paired.filter { it.use.kind == kind }
            .groupBy { it.use.key }
            .map { (key, items) ->
                val drops = items.map { it.drop }
                TreatmentEffectRow(
                    kind = kind,
                    key = key,
                    name = items.first().use.name,
                    pairedCount = items.size,
                    meanDrop = drops.average(),
                    improved = drops.count { it > 0 },
                    unchanged = drops.count { it == 0 },
                    worse = drops.count { it < 0 }
                )
            }
            .sortedBy { it.name }

    private fun bands(paired: List<Paired>, level: (Paired) -> Int?): List<BandEffect> =
        paired.mapNotNull { item ->
            val band = bandOf(level(item)) ?: return@mapNotNull null
            band to item.drop
        }.groupBy { it.first }
            .map { (band, items) ->
                BandEffect(band, items.size, items.map { it.second }.average())
            }
            .sortedBy { it.band }

    private fun bandOf(level: Int?): LevelBand? = when (level) {
        in 1..2 -> LevelBand.LOW
        in 3..5 -> LevelBand.HIGH
        else -> null
    }

    private data class Paired(val use: TreatmentUse, val drop: Int)
}
