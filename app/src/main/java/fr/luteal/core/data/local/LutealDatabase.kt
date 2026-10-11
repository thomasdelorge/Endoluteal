package fr.luteal.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.luteal.core.data.entity.BiomarkerObservationEntity
import fr.luteal.core.data.entity.CycleEntity
import fr.luteal.core.data.entity.DailyEntryEntity
import fr.luteal.core.data.entity.SymptomLogEntity
import fr.luteal.core.data.entity.UserProfileEntity

@Database(
    entities = [
        CycleEntity::class,
        DailyEntryEntity::class,
        SymptomLogEntity::class,
        UserProfileEntity::class,
        BiomarkerObservationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LutealDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun symptomDao(): SymptomDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun biomarkerDao(): BiomarkerDao
}
