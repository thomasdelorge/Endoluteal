package fr.luteal.app.calendar

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class CalendarImportScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    suspend fun enqueueNow() {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        if (!prefs.calendarImportEnabled) return
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_ONCE,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<CalendarImportWorker>().build()
        )
    }

    suspend fun schedulePeriodic() {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        val workManager = WorkManager.getInstance(context)
        if (!prefs.calendarImportEnabled) {
            workManager.cancelUniqueWork(WORK_PERIODIC)
            return
        }
        val request = PeriodicWorkRequestBuilder<CalendarImportWorker>(6, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        const val WORK_ONCE = "endoluteal-calendar-import-once"
        const val WORK_PERIODIC = "endoluteal-calendar-import-periodic"
    }
}
