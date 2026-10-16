package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.AppointmentEntity
import fr.luteal.core.data.local.AppointmentDao
import fr.luteal.core.model.professional.Appointment
import fr.luteal.core.model.professional.ReimbursementStatus
import fr.luteal.core.model.professional.ReportStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AppointmentRepository {
    fun observeAll(): Flow<List<Appointment>>
    suspend fun allOnce(): List<Appointment>
    suspend fun get(eventId: String): Appointment?
    suspend fun upsert(appointment: Appointment)
}

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao
) : AppointmentRepository {
    override fun observeAll(): Flow<List<Appointment>> =
        appointmentDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun allOnce(): List<Appointment> =
        appointmentDao.allOnce().map { it.toModel() }

    override suspend fun get(eventId: String): Appointment? =
        appointmentDao.get(eventId)?.toModel()

    override suspend fun upsert(appointment: Appointment) {
        appointmentDao.upsert(appointment.toEntity())
    }

    private fun AppointmentEntity.toModel(): Appointment = Appointment(
        eventId = eventId,
        professionalId = professionalId,
        calendarEventKey = calendarEventKey,
        location = location,
        description = description,
        report = report,
        reportStatus = ReportStatus.fromStored(reportStatus),
        titleEdited = titleEdited,
        locationEdited = locationEdited,
        feeCents = feeCents,
        reimbursementStatus = ReimbursementStatus.fromStored(reimbursementStatus),
        cpamCents = cpamCents,
        mutuelleCents = mutuelleCents
    )

    private fun Appointment.toEntity(): AppointmentEntity = AppointmentEntity(
        eventId = eventId,
        professionalId = professionalId,
        calendarEventKey = calendarEventKey,
        location = location,
        description = description,
        report = report,
        reportStatus = reportStatus.name,
        titleEdited = titleEdited,
        locationEdited = locationEdited,
        feeCents = feeCents,
        reimbursementStatus = if (reimbursementStatus == ReimbursementStatus.UNSPECIFIED) "" else reimbursementStatus.name,
        cpamCents = cpamCents,
        mutuelleCents = mutuelleCents
    )
}
