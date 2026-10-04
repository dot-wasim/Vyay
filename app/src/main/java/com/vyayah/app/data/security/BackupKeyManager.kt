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
 * Manages the "Vault Key" (Kosh Key) - the sovereign local recovery key
 * used to encrypt and restore full database backups without any cloud dependencies.
 */
object BackupKeyManager {
    private const val PREFS_NAME = "vyayah_vault_prefs"
    private const val KEY_VAULT_CODE = "vyayah_vault_key_phrase"
    private const val PBKDF2_ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Wordlist for generating human-readable 12-word Vault Keys
    private val MNEMONIC_WORDS = listOf(
        "amber", "bamboo", "cedar", "delta", "ember", "falcon", "glacier", "harbor",
        "indigo", "jasper", "kestrel", "lotus", "meadow", "nebula", "orchid", "phoenix",
        "quartz", "river", "summit", "timber", "umbra", "valiant", "willow", "zenith",
        "aurora", "breeze", "canyon", "dune", "echo", "forest", "grove", "horizon",
        "island", "jungle", "lagoon", "monarch", "oasis", "prairie", "ridge", "savanna"
    )

    fun getOrCreateVaultKey(context: Context): String {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var vaultKey = prefs.getString(KEY_VAULT_CODE, null)
        if (vaultKey == null) {
            vaultKey = generateNewVaultKey()
            prefs.edit().putString(KEY_VAULT_CODE, vaultKey).apply()
        }
        return vaultKey
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

    /**
     * Derives AES-256 SecretKey from the Vault Key using PBKDF2 with salt.
     */
    private fun deriveKey(vaultKey: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(vaultKey.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    /**
     * Encrypts plaintext data using AES-GCM with the user's Vault Key.
     */
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

        // Structure: [Salt 16B] + [IV 12B] + [Ciphertext + Tag]
        return salt + iv + ciphertext
    }

    /**
     * Decrypts ciphertext data using AES-GCM with the user's Vault Key.
     */
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
