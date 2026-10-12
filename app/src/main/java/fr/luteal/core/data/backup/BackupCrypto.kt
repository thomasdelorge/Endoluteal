package fr.luteal.core.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupCrypto {
    const val FORMAT = "endoluteal-backup-encrypted/v1"
    const val KDF = "PBKDF2WithHmacSHA256"
    const val ITERATIONS = 600_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_BITS = 128
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class Envelope(
        val format: String,
        val kdf: String,
        val iterations: Int,
        val salt: String,
        val iv: String,
        val ciphertext: String
    )

    fun isEncrypted(bytes: ByteArray): Boolean {
        return runCatching {
            val envelope = json.decodeFromString<Envelope>(bytes.decodeToString())
            envelope.format == FORMAT
        }.getOrDefault(false)
    }

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val encrypted = cipher.doFinal(plain)
        val envelope = Envelope(
            format = FORMAT,
            kdf = KDF,
            iterations = ITERATIONS,
            salt = salt.toBase64(),
            iv = iv.toBase64(),
            ciphertext = encrypted.toBase64()
        )
        return json.encodeToString(Envelope.serializer(), envelope).toByteArray(Charsets.UTF_8)
    }

    fun decrypt(bytes: ByteArray, password: CharArray): ByteArray {
        val envelope = json.decodeFromString<Envelope>(bytes.decodeToString())
        require(envelope.format == FORMAT) { "Unsupported backup encryption format" }
        require(envelope.kdf == KDF) { "Unsupported KDF" }
        val salt = envelope.salt.fromBase64()
        val iv = envelope.iv.fromBase64()
        val ciphertext = envelope.ciphertext.fromBase64()
        val key = deriveKey(password, salt, envelope.iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int = ITERATIONS): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(KDF)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun ByteArray.toBase64(): String =
        java.util.Base64.getEncoder().encodeToString(this)

    private fun String.fromBase64(): ByteArray =
        java.util.Base64.getDecoder().decode(this)
}
