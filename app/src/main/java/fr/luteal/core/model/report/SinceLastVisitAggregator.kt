package fr.luteal.core.model.report

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

data class TimelinePainSample(
    val at: Instant,
    val zone: String,
    val intensity: Int
)

data class TimelineIntakeSample(
    val at: Instant,
    val medicationId: String,
    val medicationName: String
)

data class WeeklyPainBucket(
    val weekStart: LocalDate,
    val average: Double,
    val max: Int
)

data class ZonePeak(
    val zone: String,
    val max: Int
)

data class MedicationCount(
    val medicationId: String,
    val name: String,
    val count: Int
)

data class SinceLastVisitSummary(
    val weeklyPain: List<WeeklyPainBucket>,
    val zonePeaks: List<ZonePeak>,
    val intakes: List<MedicationCount>,
    val hospitalizationCount: Int,
    val careSessionCount: Int
)

object SinceLastVisitAggregator {
    fun summarize(
        fromInclusive: Instant,
        toInclusive: Instant,
        zone: ZoneId,
        pain: List<TimelinePainSample>,
        intakes: List<TimelineIntakeSample>,
        hospitalizations: List<Instant>,
        careSessions: List<Instant>
    ): SinceLastVisitSummary {
        fun inWindow(at: Instant) = !at.isBefore(fromInclusive) && !at.isAfter(toInclusive)
        val marks = pain.filter { inWindow(it.at) }
        val weeks = WeekFields.of(Locale.FRANCE)
        val weekly = marks.groupBy { sample ->
            sample.at.atZone(zone).toLocalDate().with(weeks.dayOfWeek(), 1)
        }.map { (start, samples) ->
            val intensities = samples.map { it.intensity }
            WeeklyPainBucket(
                weekStart = start,
                average = intensities.average(),
                max = intensities.max()
            )
        }.sortedBy { it.weekStart }
        val zones = marks.groupBy { it.zone }
            .map { (name, samples) -> ZonePeak(name, samples.maxOf { it.intensity }) }
            .sortedBy { it.zone }
        val intakeCounts = intakes.filter { inWindow(it.at) }
            .groupBy { it.medicationId }
            .map { (id, rows) ->
                MedicationCount(
                    medicationId = id,
                    name = rows.first().medicationName,
                    count = rows.size
                )
            }
            .sortedBy { it.name }
        return SinceLastVisitSummary(
            weeklyPain = weekly,
            zonePeaks = zones,
            intakes = intakeCounts,
            hospitalizationCount = hospitalizations.count(::inWindow),
            careSessionCount = careSessions.count(::inWindow)
        )
    }
}
