package fr.luteal.core.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.luteal.core.data.security.KeystoreSecretStore
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoBackupPassphraseStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = KeystoreSecretStore(
        context = context,
        fileName = "auto_backup_secrets",
        keyAlias = "endoluteal_auto_backup_aes"
    )

    fun passphrase(): CharArray {
        val existing = store.get(KEY)
        if (existing != null) return existing.toCharArray()
        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encoded = Base64.getEncoder().encodeToString(random)
        store.put(KEY, encoded)
        return encoded.toCharArray()
    }

    private companion object {
        const val KEY = "passphrase"
    }
}
