package fr.luteal.app.calendar

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CalendarImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calendarImporter: CalendarImporter
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        calendarImporter.importNow()
        return Result.success()
    }
}
