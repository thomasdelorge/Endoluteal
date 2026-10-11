package fr.luteal.core.model.professional

data class Appointment(
    val eventId: String,
    val professionalId: String,
    val calendarEventKey: String? = null,
    val location: String = "",
    val report: String = "",
    val reportStatus: ReportStatus = ReportStatus.PENDING
)
