package com.vyayah.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages the "Vault Key" (Ledger Key) - the sovereign local recovery key
 * used to encrypt and restore full database backups without any cloud dependencies.
 */
object BackupKeyManager {
    private const val PREFS_NAME = "vyayah_vault_prefs"
    private const val KEY_VAULT_CODE = "vyayah_vault_key_phrase"
    private const val PBKDF2_ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    private val MNEMONIC_WORDS = listOf(
        "amber", "bamboo", "cedar", "delta", "ember", "falcon", "glacier", "harbor",
        "indigo", "jasper", "kestrel", "lotus", "meadow", "nebula", "orchid", "phoenix",
        "quartz", "river", "summit", "timber", "umbra", "valiant", "willow", "zenith",
        "aurora", "breeze", "canyon", "dune", "echo", "forest", "grove", "horizon",
        "island", "jungle", "lagoon", "monarch", "oasis", "prairie", "ridge", "savanna"
    )

    private fun getEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrCreateVaultKey(context: Context): String {
        val prefs = getEncryptedPrefs(context)
        var vaultKey = prefs.getString(KEY_VAULT_CODE, null)
        if (vaultKey == null) {
            vaultKey = generateNewVaultKey()
            prefs.edit().putString(KEY_VAULT_CODE, vaultKey).apply()
        }
        return vaultKey
    }

    /**
     * Pastes and restores an existing 12-word Ledger Key.
     */
    fun importVaultKey(context: Context, keyPhrase: String): Boolean {
        val cleanKey = keyPhrase.trim().lowercase()
        val words = cleanKey.split(Regex("\\s+"))
        if (words.size < 12) return false

        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString(KEY_VAULT_CODE, cleanKey).apply()
        return true
    }

    private fun generateNewVaultKey(): String {
        val random = SecureRandom()
        val words = mutableListOf<String>()
        repeat(12) {
            val idx = random.nextInt(MNEMONIC_WORDS.size)
            words.add(MNEMONIC_WORDS[idx])
        }
        return words.joinToString(" ")
    }

    private fun deriveKey(vaultKey: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(vaultKey.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    fun encryptWithVaultKey(vaultKey: String, data: ByteArray): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val iv = ByteArray(GCM_IV_LENGTH)
        random.nextBytes(iv)

        val secretKey = deriveKey(vaultKey, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)

        return salt + iv + ciphertext
    }

    fun decryptWithVaultKey(vaultKey: String, encryptedPayload: ByteArray): ByteArray {
        require(encryptedPayload.size > 28) { "Invalid backup file size" }
        val salt = encryptedPayload.copyOfRange(0, 16)
        val iv = encryptedPayload.copyOfRange(16, 28)
        val ciphertext = encryptedPayload.copyOfRange(28, encryptedPayload.size)

        val secretKey = deriveKey(vaultKey, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }
}
