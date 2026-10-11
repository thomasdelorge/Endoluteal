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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SymptomEntryRepositoryTest {

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
    fun symptomStoresMarksAndDeleteRemovesThem() = runTest {
        val at = Instant.parse("2026-08-01T10:00:00Z")
        repo.upsert(
            JournalEvent(
                id = "sym-1",
                type = JournalEventType.SYMPTOM,
                startAt = at,
                endAt = null,
                zoneId = "UTC",
                notes = "note",
                linkedEventId = null,
                payload = EventPayload.Symptom,
                marks = listOf(
                    PainMark("m1", "hypogastrium", 3, emptyList()),
                    PainMark("m2", "lumbar_left", 7, emptyList())
                ),
                createdAt = at,
                updatedAt = at
            )
        )
        val stored = repo.get("sym-1")
        assertEquals(7, stored?.marks?.maxOf { it.intensity })
        repo.delete("sym-1")
        assertNull(repo.get("sym-1"))
    }
}
