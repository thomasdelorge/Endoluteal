package fr.luteal.core.data.repository

import fr.luteal.core.data.entity.MedicationIntakeEntity
import fr.luteal.core.data.local.MedicationIntakeDao
import fr.luteal.core.data.local.MedicationIntakeRow
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.meds.RecordedIntake
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface MedicationIntakeRepository {
    fun observeRows(): Flow<List<MedicationIntakeRow>>
    suspend fun record(event: JournalEvent, medicationId: String, units: Double)
    suspend fun intakeForEvent(eventId: String): MedicationIntakeEntity?
    suspend fun delete(eventId: String)
    suspend fun historyUntil(until: Instant, windowStart: Instant): List<RecordedIntake>
}

@Singleton
class MedicationIntakeRepositoryImpl @Inject constructor(
    private val medicationIntakeDao: MedicationIntakeDao,
    private val journalEventRepository: JournalEventRepository
) : MedicationIntakeRepository {
    override fun observeRows(): Flow<List<MedicationIntakeRow>> = medicationIntakeDao.observeRows()

    override suspend fun intakeForEvent(eventId: String): MedicationIntakeEntity? =
        medicationIntakeDao.find(eventId)

    override suspend fun delete(eventId: String) {
        journalEventRepository.delete(eventId)
    }

    override suspend fun record(event: JournalEvent, medicationId: String, units: Double) {
        require(units > 0.0) { "Units must be positive" }
        journalEventRepository.upsert(event)
        medicationIntakeDao.upsert(
            MedicationIntakeEntity(
                eventId = event.id,
                medicationId = medicationId,
                units = units
            )
        )
    }

    override suspend fun historyUntil(until: Instant, windowStart: Instant): List<RecordedIntake> =
        medicationIntakeDao.between(windowStart.toEpochMilli(), until.toEpochMilli()).map { row ->
            RecordedIntake(
                medicationId = row.medicationId,
                units = row.units,
                at = Instant.ofEpochMilli(row.startAt)
            )
        }
}
