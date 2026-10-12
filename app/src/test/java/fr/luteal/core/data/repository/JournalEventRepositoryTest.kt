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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class JournalEventRepositoryTest {

    private val start = Instant.parse("2026-08-01T10:00:00Z")

    private lateinit var database: LutealDatabase
    private lateinit var repo: JournalEventRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = JournalEventRepositoryImpl(database.journalEventDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertReadsByRangeAndDeletesMarksInCascade() = runTest {
        repo.upsert(
            event(
                id = "evt-1",
                marks = listOf(
                    mark("m1", "hypogastre", 6),
                    mark("m2", "lombaire_g", 4),
                    mark("m3", "sacrum", 2)
                )
            )
        )

        val loaded = repo.get("evt-1")
        assertEquals(3, loaded?.marks?.size)
        assertEquals(EventPayload.PainCheck, loaded?.payload)

        val inRange = repo.observeBetween(start, start.plusSeconds(60)).first()
        assertEquals(listOf("evt-1"), inRange.map { it.id })

        val outOfRange = repo.observeBetween(start.plusSeconds(60), start.plusSeconds(120)).first()
        assertTrue(outOfRange.isEmpty())

        repo.delete("evt-1")
        assertNull(repo.get("evt-1"))
        assertTrue(database.journalEventDao().getAllMarksOnce().isEmpty())
    }

    @Test
    fun upsertPersistsPinCoordinates() = runTest {
        val pin = mark("pin-1", "f08", 5).copy(
            pinSide = "front",
            pinX = 0.42f,
            pinY = 0.67f
        )
        repo.upsert(event(id = "evt-pin", marks = listOf(pin)))

        val loaded = repo.get("evt-pin")?.marks?.single()
        assertEquals("front", loaded?.pinSide)
        assertEquals(0.42f, loaded?.pinX)
        assertEquals(0.67f, loaded?.pinY)
        assertTrue(loaded?.isPin == true)
    }

    private fun event(id: String, marks: List<PainMark>) = JournalEvent(
        id = id,
        type = JournalEventType.PAIN_CHECK,
        startAt = start,
        endAt = null,
        zoneId = "Europe/Paris",
        notes = "",
        linkedEventId = null,
        payload = EventPayload.PainCheck,
        marks = marks,
        createdAt = start,
        updatedAt = start
    )

    private fun mark(id: String, zone: String, intensity: Int) = PainMark(
        id = id,
        zone = zone,
        intensity = intensity,
        painTypes = listOf("crampe")
    )
}
