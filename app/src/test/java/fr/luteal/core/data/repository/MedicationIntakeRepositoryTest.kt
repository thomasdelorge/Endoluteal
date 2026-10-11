package fr.luteal.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.model.EventPayload
import fr.luteal.core.model.JournalEvent
import fr.luteal.core.model.JournalEventType
import fr.luteal.core.model.PainMark
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MedicationIntakeRepositoryTest {

    private val at = Instant.parse("2026-08-01T10:00:00Z")
    private lateinit var database: LutealDatabase
    private lateinit var journal: JournalEventRepositoryImpl
    private lateinit var intakes: MedicationIntakeRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        journal = JournalEventRepositoryImpl(database.journalEventDao())
        intakes = MedicationIntakeRepositoryImpl(database.medicationIntakeDao(), journal)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordsConfirmedIntakeAndLinkedPainCheck() = runTest {
        val intake = event("prise", JournalEventType.MEDICATION_INTAKE, EventPayload.MedicationIntake(dangerAcknowledged = true), null)
        intakes.record(intake, "med-paracetamol", 2.0)
        val check = event("controle", JournalEventType.PAIN_CHECK, EventPayload.PainCheck, null)
            .copy(
                linkedEventId = "prise",
                marks = listOf(PainMark("m2", "global", 4, emptyList()))
            )
        journal.upsert(check)

        val stored = journal.get("prise")
        assertEquals(true, (stored?.payload as EventPayload.MedicationIntake).dangerAcknowledged)
        assertEquals(6, stored.marks.single().intensity)
        assertEquals(2.0, database.medicationIntakeDao().allOnce().single().units, 0.01)
        assertEquals("prise", journal.get("controle")?.linkedEventId)
        assertTrue(intakes.historyUntil(at, at.minusSeconds(3600)).any { it.medicationId == "med-paracetamol" })
    }

    private fun event(id: String, type: JournalEventType, payload: EventPayload, linked: String?) = JournalEvent(
        id = id,
        type = type,
        startAt = at,
        endAt = null,
        zoneId = "Europe/Paris",
        notes = "",
        linkedEventId = linked,
        payload = payload,
        marks = listOf(PainMark("m1", "global", 6, emptyList())),
        createdAt = at,
        updatedAt = at
    )
}
