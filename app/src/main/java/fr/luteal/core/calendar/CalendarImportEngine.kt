package fr.luteal.core.calendar

import fr.luteal.core.model.professional.ReportStatus

data class ImportedCalendarInstance(
    val calendarEventKey: String,
    val title: String,
    val description: String,
    val location: String,
    val startAtEpochMillis: Long,
    val endAtEpochMillis: Long?
)

data class ExistingImportedAppointment(
    val eventId: String,
    val calendarEventKey: String,
    val professionalId: String,
    val location: String,
    val report: String,
    val reportStatus: ReportStatus,
    val title: String,
    val startAtEpochMillis: Long,
    val endAtEpochMillis: Long?
)

data class ProfessionalSuggestionHistory(
    val professionalId: String,
    val title: String,
    val location: String,
    val startAtEpochMillis: Long
)

data class CalendarImportUpsert(
    val existingEventId: String?,
    val calendarEventKey: String,
    val title: String,
    val location: String,
    val professionalId: String,
    val startAtEpochMillis: Long,
    val endAtEpochMillis: Long?,
    val reportStatus: ReportStatus
)

data class CalendarImportPlan(
    val upserts: List<CalendarImportUpsert>,
    val cancelEventIds: List<String>
)

object CalendarImportEngine {
    fun calendarEventKey(eventId: Long, beginMillis: Long): String = "$eventId:$beginMillis"

    fun planImport(
        instances: List<ImportedCalendarInstance>,
        existingImported: List<ExistingImportedAppointment>,
        history: List<ProfessionalSuggestionHistory>
    ): CalendarImportPlan {
        val importedKeys = instances.map { it.calendarEventKey }.toSet()
        val existingByKey = existingImported.associateBy { it.calendarEventKey }

        val upserts = instances.map { instance ->
            val prior = existingByKey[instance.calendarEventKey]
            val professionalId = prior?.professionalId?.takeIf { it.isNotBlank() }
                ?: suggestProfessionalId(instance.title, instance.location, history)
                ?: ""
            val reportStatus = when {
                prior == null -> ReportStatus.PENDING
                prior.reportStatus == ReportStatus.CANCELLED && prior.report.isBlank() -> ReportStatus.PENDING
                else -> prior.reportStatus
            }
            CalendarImportUpsert(
                existingEventId = prior?.eventId,
                calendarEventKey = instance.calendarEventKey,
                title = instance.title,
                location = instance.location,
                professionalId = professionalId,
                startAtEpochMillis = instance.startAtEpochMillis,
                endAtEpochMillis = instance.endAtEpochMillis,
                reportStatus = reportStatus
            )
        }

        val cancelEventIds = existingImported
            .filter { it.calendarEventKey !in importedKeys }
            .filter { it.report.isBlank() && it.reportStatus != ReportStatus.DONE }
            .map { it.eventId }

        return CalendarImportPlan(upserts = upserts, cancelEventIds = cancelEventIds)
    }

    fun suggestProfessionalId(
        title: String,
        location: String,
        history: List<ProfessionalSuggestionHistory>
    ): String? {
        val normalizedTitle = title.trim().lowercase()
        val normalizedLocation = location.trim().lowercase()
        return history
            .sortedByDescending { it.startAtEpochMillis }
            .firstOrNull { entry ->
                entry.professionalId.isNotBlank() && (
                    (normalizedTitle.isNotEmpty() && entry.title.trim().lowercase() == normalizedTitle) ||
                        (normalizedLocation.isNotEmpty() && entry.location.trim().lowercase() == normalizedLocation)
                )
            }
            ?.professionalId
    }
}
