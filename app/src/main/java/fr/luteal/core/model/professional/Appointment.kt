package fr.luteal.core.model.professional

data class Appointment(
    val eventId: String,
    val professionalId: String,
    val calendarEventKey: String? = null,
    val location: String = "",
    val description: String = "",
    val report: String = "",
    val reportStatus: ReportStatus = ReportStatus.PENDING,
    val titleEdited: Boolean = false,
    val locationEdited: Boolean = false,
    val feeCents: Int? = null,
    val reimbursementStatus: ReimbursementStatus = ReimbursementStatus.UNSPECIFIED,
    val cpamCents: Int? = null,
    val mutuelleCents: Int? = null
)
