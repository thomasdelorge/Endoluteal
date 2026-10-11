package fr.luteal.app.backup

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.luteal.app.R
import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.designsystem.component.LutealCard
import fr.luteal.core.designsystem.component.LutealToggleRow
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AutoBackupViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val backupWriter: BackupWriter
) : ViewModel() {
    val preferences = userPreferencesDataStore.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fr.luteal.core.data.datastore.UserPreferences())

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setAutoBackupEnabled(enabled)
            backupWriter.reschedule()
        }
    }

    fun setUri(uri: String) {
        viewModelScope.launch {
            userPreferencesDataStore.setAutoBackupUri(uri)
            backupWriter.reschedule()
        }
    }

    fun setHour(hour: String) {
        viewModelScope.launch {
            userPreferencesDataStore.setAutoBackupHour(hour)
            backupWriter.reschedule()
        }
    }

    fun setKeep(keep: Int) {
        viewModelScope.launch { userPreferencesDataStore.setAutoBackupKeep(keep) }
    }

    fun backupNow() {
        viewModelScope.launch { backupWriter.writeNow() }
    }
}

@Composable
fun AutoBackupCard(viewModel: AutoBackupViewModel = hiltViewModel()) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setUri(uri.toString())
        }
    }
    LutealCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            LutealToggleRow(
                title = stringResource(R.string.backup_auto_title),
                description = stringResource(R.string.backup_auto_body),
                checked = prefs.autoBackupEnabled,
                onCheckedChange = viewModel::setEnabled
            )
            TextButton(onClick = { folderLauncher.launch(null) }) {
                Text(stringResource(R.string.backup_choose_folder))
            }
            OutlinedTextField(
                value = prefs.autoBackupHour,
                onValueChange = viewModel::setHour,
                label = { Text(stringResource(R.string.backup_hour)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = prefs.autoBackupKeep.toString(),
                onValueChange = { raw -> raw.toIntOrNull()?.let(viewModel::setKeep) },
                label = { Text(stringResource(R.string.backup_keep)) },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = viewModel::backupNow) { Text(stringResource(R.string.backup_now)) }
            if (prefs.autoBackupLastSuccess.isNotBlank()) {
                Text(stringResource(R.string.backup_last_success, prefs.autoBackupLastSuccess))
            }
            if (prefs.autoBackupLastError.isNotBlank()) {
                Text(stringResource(R.string.backup_last_error, prefs.autoBackupLastError))
            }
        }
    }
}
