package fr.luteal.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.luteal.core.data.local.BiomarkerDao
import fr.luteal.core.data.local.CycleDao
import fr.luteal.core.data.local.DailyEntryDao
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.data.local.SymptomDao
import fr.luteal.core.data.local.UserProfileDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLutealDatabase(
        @ApplicationContext context: Context
    ): LutealDatabase {
        return Room.databaseBuilder(
            context,
            LutealDatabase::class.java,
            "endoluteal.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideCycleDao(database: LutealDatabase): CycleDao = database.cycleDao()

    @Provides
    @Singleton
    fun provideDailyEntryDao(database: LutealDatabase): DailyEntryDao = database.dailyEntryDao()

    @Provides
    @Singleton
    fun provideSymptomDao(database: LutealDatabase): SymptomDao = database.symptomDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: LutealDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    @Singleton
    fun provideBiomarkerDao(database: LutealDatabase): BiomarkerDao = database.biomarkerDao()

    @Provides
    @Singleton
    fun provideJournalEventDao(database: LutealDatabase): JournalEventDao = database.journalEventDao()
}
