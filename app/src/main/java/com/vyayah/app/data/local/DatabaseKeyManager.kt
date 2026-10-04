package com.vyayah.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DatabaseKeyManager {
    private const val PREFS_NAME = "vyayah_secure_prefs"
    private const val KEY_PASSPHRASE = "db_sqlcipher_passphrase"
    private const val KEY_LENGTH_BYTES = 32

    fun getDatabasePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphraseHex = sharedPreferences.getString(KEY_PASSPHRASE, null)
        if (passphraseHex == null) {
            val randomBytes = ByteArray(KEY_LENGTH_BYTES)
            SecureRandom().nextBytes(randomBytes)
            passphraseHex = randomBytes.joinToString("") { "%02x".format(it) }
            sharedPreferences.edit().putString(KEY_PASSPHRASE, passphraseHex).apply()
        }

        return passphraseHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
