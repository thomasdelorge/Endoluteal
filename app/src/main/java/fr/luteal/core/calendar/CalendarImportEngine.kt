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
        val existingByKey = existingImported.associateBy { it.calendarEventKey }
        val claimed = mutableSetOf<String>()

        val upserts = instances.map { instance ->
            val exact = existingByKey[instance.calendarEventKey]
            val prior = exact ?: uniqueByAndroidEventId(instance.calendarEventKey, existingImported, claimed)
            if (prior != null) claimed += prior.eventId
            if (prior != null) {
                CalendarImportUpsert(
                    existingEventId = prior.eventId,
                    calendarEventKey = instance.calendarEventKey,
                    title = instance.title,
                    location = instance.location,
                    professionalId = prior.professionalId,
                    startAtEpochMillis = prior.startAtEpochMillis,
                    endAtEpochMillis = prior.endAtEpochMillis,
                    reportStatus = prior.reportStatus
                )
            } else {
                CalendarImportUpsert(
                    existingEventId = null,
                    calendarEventKey = instance.calendarEventKey,
                    title = instance.title,
                    location = instance.location,
                    professionalId = suggestProfessionalId(instance.title, instance.location, history).orEmpty(),
                    startAtEpochMillis = instance.startAtEpochMillis,
                    endAtEpochMillis = instance.endAtEpochMillis,
                    reportStatus = ReportStatus.PENDING
                )
            }
        }

        return CalendarImportPlan(upserts = upserts, cancelEventIds = emptyList())
    }

    private fun uniqueByAndroidEventId(
        calendarEventKey: String,
        existingImported: List<ExistingImportedAppointment>,
        claimed: Set<String>
    ): ExistingImportedAppointment? {
        val androidId = calendarEventKey.substringBefore(':')
        val candidates = existingImported.filter {
            it.eventId !in claimed && it.calendarEventKey.substringBefore(':') == androidId
        }
        return candidates.singleOrNull()
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
