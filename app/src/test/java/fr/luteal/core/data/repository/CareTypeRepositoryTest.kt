package fr.luteal.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.model.care.CareProgram
import fr.luteal.core.model.care.CareType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CareTypeRepositoryTest {

    private lateinit var database: LutealDatabase
    private lateinit var repository: CareTypeRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CareTypeRepositoryImpl(database.careTypeDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun programDefaultDurationIsStored() = runTest {
        repository.upsert(
            CareType(
                id = "care-tens",
                name = "TENS",
                programs = listOf(CareProgram("TENS A", 20)),
                archived = false
            )
        )
        val loaded = repository.allOnce().single()
        assertEquals(20, loaded.programs.single { it.name == "TENS A" }.defaultDurationMinutes)
    }
}
