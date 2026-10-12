package fr.luteal.core.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {

    @Test
    fun roundTripAndWrongPasswordFails() {
        val plain = """{"backup_format":"endoluteal/v1"}""".toByteArray()
        val encrypted = BackupCrypto.encrypt(plain, "correct".toCharArray())
        assertTrue(BackupCrypto.isEncrypted(encrypted))
        val decrypted = BackupCrypto.decrypt(encrypted, "correct".toCharArray())
        assertArrayEquals(plain, decrypted)
        assertFalse(
            runCatching { BackupCrypto.decrypt(encrypted, "wrong".toCharArray()) }.isSuccess
        )
    }
}
