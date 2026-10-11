package fr.luteal.core.data

import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.local.LutealDatabase
import fr.luteal.core.data.security.PinCryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a complete, GDPR-compliant local wipe of all user health records,
 * settings, and PIN secrets stored on device.
 */
@Singleton
class LocalDataPurgeManager @Inject constructor(
    private val database: LutealDatabase,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val pinCryptoManager: PinCryptoManager
) {
    suspend fun purgeAllLocalData(): Boolean {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        userPreferencesDataStore.clear()
        return runCatching { pinCryptoManager.clearPinTracked() }.getOrDefault(false)
    }
}
