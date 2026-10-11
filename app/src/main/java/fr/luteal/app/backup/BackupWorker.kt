package fr.luteal.app.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupWriter: BackupWriter
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val written = backupWriter.writeNow()
        backupWriter.reschedule()
        return if (written.isSuccess) Result.success() else Result.failure()
    }
}
