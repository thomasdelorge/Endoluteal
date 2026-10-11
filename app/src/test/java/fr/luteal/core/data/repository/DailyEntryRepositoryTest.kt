package fr.luteal.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.model.BleedingIntensity
import fr.luteal.core.model.DailyEntry
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DailyEntryRepositoryTest {

    private val fixedInstant = Instant.parse("2026-08-13T12:00:00Z")

    private lateinit var database: LutealDatabase
    private lateinit var repo: DailyEntryRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = DailyEntryRepositoryImpl(database.dailyEntryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entry(date: String) = DailyEntry(
        date = LocalDate.parse(date),
        bleedingIntensity = BleedingIntensity.MEDIUM,
        updatedAt = fixedInstant
    )

    @Test
    fun saveInsertsRow() = runTest {
        repo.save(entry("2026-08-10"))
        assertNotNull(database.dailyEntryDao().getEntryOnce("2026-08-10"))
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val date = LocalDate.parse("2026-08-10")
        repo.save(entry(date.toString()))
        repo.delete(date)
        assertNull(database.dailyEntryDao().getEntryOnce(date.toString()))
    }

    @Test
    fun saveAfterDeleteRestoresRow() = runTest {
        val date = LocalDate.parse("2026-08-10")
        repo.save(entry(date.toString()))
        repo.delete(date)
        repo.save(entry(date.toString()))
        assertNotNull(database.dailyEntryDao().getEntryOnce(date.toString()))
    }
}
