package fr.luteal.core.model.report

import fr.luteal.core.model.professional.Specialty
import java.time.Instant

data class TimelineVisit(
    val eventId: String,
    val kind: TimelineVisitKind,
    val professionalId: String?,
    val specialty: Specialty?,
    val at: Instant,
    val title: String,
    val subtitle: String,
    val detail: String
)

enum class TimelineVisitKind {
    APPOINTMENT,
    HOSPITALIZATION
}

object TimelineFilter {
    fun apply(
        visits: List<TimelineVisit>,
        professionalId: String?,
        specialty: Specialty?,
        fromInclusive: Instant?,
        toInclusive: Instant?
    ): List<TimelineVisit> {
        return visits.filter { visit ->
            val professionalOk = professionalId == null ||
                (visit.kind == TimelineVisitKind.APPOINTMENT && visit.professionalId == professionalId)
            val specialtyOk = specialty == null || visit.specialty == specialty
            val after = fromInclusive == null || !visit.at.isBefore(fromInclusive)
            val before = toInclusive == null || !visit.at.isAfter(toInclusive)
            professionalOk && specialtyOk && after && before
        }.sortedByDescending { it.at }
    }
}
