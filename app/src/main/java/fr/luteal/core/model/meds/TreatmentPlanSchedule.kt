package fr.luteal.core.model.meds

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TreatmentPlan(
    val id: String,
    val medicationId: String,
    val unitsPerDose: Double,
    val timesOfDay: List<LocalTime>,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val active: Boolean,
    val continuousHormonal: Boolean
)

data class DueDose(
    val planId: String,
    val medicationId: String,
    val units: Double
)

data class GroupedIntake(
    val at: LocalDateTime,
    val doses: List<DueDose>
)

fun nextGroupedIntake(plans: List<TreatmentPlan>, now: LocalDateTime): GroupedIntake? {
    val active = plans.filter { it.active && it.timesOfDay.isNotEmpty() && it.unitsPerDose > 0.0 }
    var day = now.toLocalDate()
    val horizon = day.plusDays(370)
    while (day.isBefore(horizon)) {
        val due = active.filter { plan ->
            !day.isBefore(plan.startDate) && (plan.endDate == null || day.isBefore(plan.endDate))
        }
        val times = due.flatMap { plan -> plan.timesOfDay.map { time -> time to plan } }
            .groupBy({ it.first }, { it.second })
        val nextTime = times.keys.filter { time -> !day.atTime(time).isBefore(now) }.minOrNull()
        if (nextTime != null) {
            val doses = times.getValue(nextTime).map { plan ->
                DueDose(plan.id, plan.medicationId, plan.unitsPerDose)
            }
            return GroupedIntake(day.atTime(nextTime), doses)
        }
        day = day.plusDays(1)
    }
    return null
}

fun continuousHormonalActive(plans: List<TreatmentPlan>, today: LocalDate): Boolean =
    plans.any { plan ->
        plan.active &&
            plan.continuousHormonal &&
            !today.isBefore(plan.startDate) &&
            (plan.endDate == null || today.isBefore(plan.endDate))
    }
