package fr.luteal.app.calendar

import fr.luteal.core.calendar.CalendarEventFilter
import fr.luteal.core.calendar.CalendarImportEngine
import fr.luteal.core.calendar.ExistingImportedAppointment
import fr.luteal.core.calendar.ImportedCalendarInstance
import fr.luteal.core.calendar.ProfessionalSuggestionHistory
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.local.AppointmentDao
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.app.notification.AppointmentReportScheduler
import fr.luteal.core.data.repository.AppointmentRepository
import fr.luteal.core.data.repository.JournalEventRepository
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.professional.Appointment
import fr.luteal.core.model.professional.ReportStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class CalendarImporter @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val instancesReader: CalendarInstancesReader,
    private val appointmentDao: AppointmentDao,
    private val journalEventDao: JournalEventDao,
    private val journalEventRepository: JournalEventRepository,
    private val appointmentRepository: AppointmentRepository,
    private val appointmentReportScheduler: AppointmentReportScheduler,
    private val clock: Clock
) {
    suspend fun importNow(): Result<Int> = runCatching {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        if (!prefs.calendarImportEnabled) return@runCatching 0
        val keywords = CalendarEventFilter.parseKeywords(prefs.calendarImportKeywords)
        val calendarIds = prefs.calendarSelectedIds.mapNotNull { it.toLongOrNull() }.toSet()
        val zone = ZoneId.systemDefault()
        val now = clock.instant()
        val from = now.minus(30, ChronoUnit.DAYS)
        val to = now.plus(90, ChronoUnit.DAYS)
        val rawInstances = instancesReader.readInstances(calendarIds, from, to)
        val instances = rawInstances.filter { instance ->
            CalendarEventFilter.matches(instance.title, instance.description, keywords)
        }
        val eventsById = journalEventDao.getAllOnce()
            .filter { it.type == JournalEventType.APPOINTMENT.name }
            .associateBy { it.id }
        val existingImported = appointmentDao.allWithCalendarKeyOnce().mapNotNull { entity ->
            val event = eventsById[entity.eventId] ?: return@mapNotNull null
            ExistingImportedAppointment(
                eventId = entity.eventId,
                calendarEventKey = entity.calendarEventKey ?: return@mapNotNull null,
                professionalId = entity.professionalId,
                location = entity.location,
                report = entity.report,
                reportStatus = ReportStatus.fromStored(entity.reportStatus),
                title = event.notes,
                startAtEpochMillis = event.startAt,
                endAtEpochMillis = event.endAt
            )
        }
        val history = buildHistory(eventsById, appointmentDao.allOnce())
        val plan = CalendarImportEngine.planImport(instances, existingImported, history)
        val nowInstant = clock.instant()
        plan.upserts.forEach { upsert ->
            val eventId = persistUpsert(upsert, zone, nowInstant)
            appointmentReportScheduler.syncForEvent(eventId)
        }
        plan.cancelEventIds.forEach { eventId ->
            val existing = appointmentRepository.get(eventId) ?: return@forEach
            appointmentRepository.upsert(
                existing.copy(reportStatus = ReportStatus.CANCELLED)
            )
            appointmentReportScheduler.syncForEvent(eventId)
        }
        plan.upserts.size
    }

    private suspend fun persistUpsert(
        upsert: fr.luteal.core.calendar.CalendarImportUpsert,
        zone: ZoneId,
        now: Instant
    ): String {
        val eventId = upsert.existingEventId ?: UUID.randomUUID().toString()
        val existingEvent = upsert.existingEventId?.let { journalEventRepository.get(it) }
        val existingAppointment = appointmentRepository.get(eventId)
        // A local edit of the title or location wins over the calendar on the
        // next sync: keep the user's wording instead of the calendar's.
        val titleEdited = existingAppointment?.titleEdited == true
        if (existingEvent != null) {
            journalEventRepository.upsert(
                existingEvent.copy(
                    notes = if (titleEdited) existingEvent.notes else upsert.title,
                    updatedAt = now
                )
            )
        } else {
            val startAt = Instant.ofEpochMilli(upsert.startAtEpochMillis)
            val endAt = upsert.endAtEpochMillis?.let(Instant::ofEpochMilli)
            journalEventRepository.upsert(
                JournalEvent(
                    id = eventId,
                    type = JournalEventType.APPOINTMENT,
                    startAt = startAt,
                    endAt = endAt,
                    zoneId = zone.id,
                    notes = upsert.title,
                    linkedEventId = null,
                    payload = EventPayload.Appointment,
                    marks = emptyList(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        if (existingAppointment != null) {
            appointmentRepository.upsert(
                existingAppointment.copy(
                    location = if (existingAppointment.locationEdited) existingAppointment.location else upsert.location,
                    calendarEventKey = upsert.calendarEventKey
                )
            )
        } else {
            appointmentRepository.upsert(
                Appointment(
                    eventId = eventId,
                    professionalId = upsert.professionalId,
                    calendarEventKey = upsert.calendarEventKey,
                    location = upsert.location,
                    reportStatus = upsert.reportStatus
                )
            )
        }
        return eventId
    }

    private fun buildHistory(
        eventsById: Map<String, fr.luteal.core.data.entity.JournalEventEntity>,
        appointments: List<fr.luteal.core.data.entity.AppointmentEntity>
    ): List<ProfessionalSuggestionHistory> {
        return appointments.mapNotNull { entity ->
            val event = eventsById[entity.eventId] ?: return@mapNotNull null
            ProfessionalSuggestionHistory(
                professionalId = entity.professionalId,
                title = event.notes,
                location = entity.location,
                startAtEpochMillis = event.startAt
            )
        }
    }
}
