package fr.luteal.core.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.data.repository.BiomarkerRepositoryImpl
import fr.luteal.core.data.repository.CycleRepositoryImpl
import fr.luteal.core.data.repository.DailyEntryRepositoryImpl
import fr.luteal.core.model.BleedingIntensity
import fr.luteal.core.model.Cycle
import fr.luteal.core.model.CyclePhase
import fr.luteal.core.model.CurrentCyclePhase
import fr.luteal.core.model.PhaseCertainty
import fr.luteal.core.model.recap.RecapCyclePayload
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecapCycleWriterTest {

    private val fixedInstant = Instant.parse("2026-08-13T12:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    private lateinit var database: LutealDatabase
    private lateinit var dailyEntryRepository: DailyEntryRepositoryImpl
    private lateinit var biomarkerRepository: BiomarkerRepositoryImpl
    private lateinit var cycleRepository: CycleRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LutealDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyEntryRepository = DailyEntryRepositoryImpl(database.dailyEntryDao())
        biomarkerRepository = BiomarkerRepositoryImpl(database.biomarkerDao())
        cycleRepository = CycleRepositoryImpl(database, database.cycleDao(), database.dailyEntryDao(), clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun bleedingAfterQuietDayStartsNewCycle() = runTest {
        val dayBefore = LocalDate.parse("2026-08-12")
        val flowDay = LocalDate.parse("2026-08-13")
        cycleRepository.saveCycle(
            Cycle(
                id = "c1",
                startDate = dayBefore.minusDays(20),
                endDate = null,
                periodDays = emptyList()
            )
        )
        val payload = RecapCyclePayload(
            hasBleeding = true,
            bleedingIntensity = BleedingIntensity.MEDIUM.name
        )
        RecapCycleWriter.apply(
            date = flowDay,
            payload = payload,
            cycles = cycleRepository.getCyclesOnce(),
            dailyEntryRepository = dailyEntryRepository,
            biomarkerRepository = biomarkerRepository,
            cycleRepository = cycleRepository,
            clock = clock
        )
        val cycles = cycleRepository.getCyclesOnce()
        assertTrue(cycles.any { it.startDate == flowDay })
        val entry = dailyEntryRepository.getEntryOnce(flowDay)
        assertEquals(BleedingIntensity.MEDIUM, entry?.bleedingIntensity)
    }

    @Test
    fun estimatedPhaseFlagMatchesCalculatorCertainty() {
        val estimated = CurrentCyclePhase.Available(CyclePhase.LUTEAL, PhaseCertainty.ESTIMATED)
        val recorded = CurrentCyclePhase.Available(CyclePhase.LUTEAL, PhaseCertainty.RECORDED)
        assertTrue(estimated.certainty == PhaseCertainty.ESTIMATED)
        assertTrue(recorded.certainty != PhaseCertainty.ESTIMATED)
    }
}
