package fr.luteal.app.backup

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.app.MainActivity
import fr.luteal.app.R
import fr.luteal.app.notification.NotificationChannelManager
import fr.luteal.core.data.DataExportManager
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
class BackupWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataExportManager: DataExportManager,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    suspend fun writeNow(): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        if (prefs.autoBackupUri.isBlank()) {
            val message = context.getString(R.string.backup_error_folder)
            userPreferencesDataStore.setAutoBackupStatus(success = prefs.autoBackupLastSuccess, error = message)
            fail(message)
        } else {
            runCatching { write(prefs.autoBackupUri, prefs.autoBackupKeep) }
                .onSuccess {
                    userPreferencesDataStore.setAutoBackupStatus(
                        success = LocalDateTime.now().toString(),
                        error = ""
                    )
                }
                .onFailure { error ->
                    val message = error.message ?: context.getString(R.string.backup_error_generic)
                    userPreferencesDataStore.setAutoBackupStatus(success = prefs.autoBackupLastSuccess, error = message)
                    notifyFailure(message)
                }
        }
    }

    suspend fun reschedule() {
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        val workManager = WorkManager.getInstance(context)
        if (!prefs.autoBackupEnabled || prefs.autoBackupUri.isBlank()) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val target = runCatching { LocalTime.parse(prefs.autoBackupHour) }.getOrDefault(LocalTime.of(3, 0))
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(target)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInitialDelay(Duration.between(now, next))
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private suspend fun write(tree: String, keep: Int) {
        val resolver = context.contentResolver
        val treeUri = Uri.parse(tree)
        val parent = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
        val name = "endoluteal-backup-${LocalDateTime.now().format(STAMP)}.json"
        val created = DocumentsContract.createDocument(resolver, parent, "application/json", name)
            ?: error(context.getString(R.string.backup_error_folder))
        resolver.openOutputStream(created)?.use { stream ->
            dataExportManager.exportToStream(stream)
        } ?: error(context.getString(R.string.backup_error_folder))
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
        val found = mutableListOf<Pair<String, String>>()
        resolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                found += cursor.getString(idIndex) to cursor.getString(nameIndex)
            }
        }
        backupsToDelete(found.map { it.second }, keep).forEach { displayName ->
            val documentId = found.first { it.second == displayName }.first
            DocumentsContract.deleteDocument(
                resolver,
                DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            )
        }
    }

    private fun fail(message: String): Result<Unit> {
        notifyFailure(message)
        return Result.failure(IllegalStateException(message))
    }

    private fun notifyFailure(message: String) {
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_BACKUP)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.backup_failed_title))
            .setContentText(message)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(BACKUP_NOTIFICATION, notification)
    }

    companion object {
        const val WORK_NAME = "endoluteal-auto-backup"
        private const val BACKUP_NOTIFICATION = 4401
        private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
    }
}
