package fr.luteal.core.domain

import fr.luteal.core.data.repository.BiomarkerRepository
import fr.luteal.core.data.repository.CycleRepository
import fr.luteal.core.data.repository.DailyEntryRepository
import fr.luteal.core.model.BiomarkerObservation
import fr.luteal.core.model.BleedingIntensity
import fr.luteal.core.model.CervicalFluidObservation
import fr.luteal.core.model.CervicalMucusTexture
import fr.luteal.core.model.Cycle
import fr.luteal.core.model.DailyEntry
import fr.luteal.core.model.recap.RecapCyclePayload
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

object RecapCycleWriter {
    suspend fun apply(
        date: LocalDate,
        payload: RecapCyclePayload,
        cycles: List<Cycle>,
        dailyEntryRepository: DailyEntryRepository,
        biomarkerRepository: BiomarkerRepository,
        cycleRepository: CycleRepository,
        clock: Clock
    ) {
        val now = clock.instant()
        val bleeding = payload.bleedingIntensity.takeIf { payload.hasBleeding && it.isNotBlank() }
            ?.let { runCatching { BleedingIntensity.valueOf(it) }.getOrNull() }
        val previous = dailyEntryRepository.getEntryOnce(date.minusDays(1))
        val hadFlowYesterday = previous?.bleedingIntensity?.let { it != BleedingIntensity.NONE } == true
        val startsNewCycle = bleeding != null && bleeding != BleedingIntensity.NONE && !hadFlowYesterday

        val existing = dailyEntryRepository.getEntryOnce(date)
        dailyEntryRepository.save(
            (existing ?: DailyEntry(date = date)).copy(
                bleedingIntensity = bleeding,
                updatedAt = now
            )
        )

        if (payload.hasCervicalMucus && payload.mucusTexture.isNotBlank()) {
            val texture = runCatching { CervicalMucusTexture.valueOf(payload.mucusTexture) }.getOrNull()
            if (texture != null) {
                val biomarker = biomarkerRepository.getObservationOnce(date) ?: BiomarkerObservation(date = date)
                biomarkerRepository.save(
                    biomarker.copy(
                        cervicalFluid = CervicalFluidObservation(texture = texture),
                        updatedAt = now
                    )
                )
            }
        }

        if (!startsNewCycle || bleeding == null || bleeding == BleedingIntensity.NONE) return
        val current = cycles.firstOrNull { cycle ->
            cycle.startDate <= date && (cycle.endDate == null || cycle.endDate >= date)
        }
        if (current != null && current.startDate < date) {
            cycleRepository.saveCycle(current.copy(endDate = date.minusDays(1)))
        }
        if (cycles.none { it.startDate == date }) {
            cycleRepository.addBackfilledCycle(date)
        }
    }
}
