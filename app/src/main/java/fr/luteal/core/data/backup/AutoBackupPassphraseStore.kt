package fr.luteal.core.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.security.KeystoreSecretStore
import javax.inject.Inject
import javax.inject.Singleton

/** Thrown when encryption is on but the user has not stored a passphrase. */
class MissingBackupPassphrase : IllegalStateException("Backup encryption is enabled but no passphrase is stored")

@Singleton
class AutoBackupPassphraseStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = KeystoreSecretStore(
        context = context,
        fileName = "auto_backup_secrets",
        keyAlias = "endoluteal_auto_backup_aes"
    )

    /** Stored user passphrase, or null when none has been set. */
    fun passphrase(): CharArray? = store.get(KEY)?.toCharArray()

    fun setPassphrase(value: CharArray) {
        require(value.isNotEmpty())
        store.put(KEY, String(value))
    }

    fun clear() {
        store.remove(KEY)
    }

    private companion object {
        const val KEY = "passphrase"
    }
}
