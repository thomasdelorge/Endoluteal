package fr.luteal.core.model.report

import fr.luteal.core.model.professional.Specialty
import java.time.Instant

data class TimelinePdfContent(
    val visits: List<TimelineVisit>,
    val summary: SinceLastVisitSummary?
)

fun buildTimelinePdfContent(
    visits: List<TimelineVisit>,
    professionalId: String?,
    specialty: Specialty?,
    fromInclusive: Instant?,
    toInclusive: Instant?,
    summary: SinceLastVisitSummary?
): TimelinePdfContent = TimelinePdfContent(
    visits = TimelineFilter.apply(visits, professionalId, specialty, fromInclusive, toInclusive),
    summary = if (professionalId == null) null else summary
)
