package com.vyayah.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class VaultBackupPayload(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val goals: List<Goal> = emptyList()
)

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
    const val BACKUP_FILENAME = "vyayah_vault_backup.vyayah"

    private val jsonHelper = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val MNEMONIC_WORDS = listOf(
        "amber", "bamboo", "cedar", "delta", "ember", "falcon", "glacier", "harbor",
        "indigo", "jasper", "kestrel", "lotus", "meadow", "nebula", "orchid", "phoenix",
        "quartz", "river", "summit", "timber", "umbra", "valiant", "willow", "zenith",
        "aurora", "breeze", "canyon", "dune", "echo", "forest", "grove", "horizon",
        "island", "jungle", "lagoon", "monarch", "oasis", "prairie", "ridge", "savanna"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences("vyayah_vault_fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    fun getOrCreateVaultKey(context: Context): String {
        val prefs = getPrefs(context)
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

        val prefs = getPrefs(context)
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

    suspend fun createEncryptedBackup(context: Context, database: VyayahDatabase, vaultKey: String): File? {
        return try {
            val accounts = database.accountDao().getAllAccountsSnapshot()
            val transactions = database.transactionDao().getAllTransactionsSnapshot()
            val categories = database.categoryDao().getAllCategoriesSnapshot()
            val budgets = database.budgetDao().getAllBudgetsSnapshot()
            val goals = database.goalDao().getAllGoalsSnapshot()

            val payload = VaultBackupPayload(
                accounts = accounts,
                transactions = transactions,
                categories = categories,
                budgets = budgets,
                goals = goals
            )

            val jsonString = jsonHelper.encodeToString(payload)
            val encryptedBytes = encryptWithVaultKey(vaultKey, jsonString.toByteArray(Charsets.UTF_8))

            val internalFile = File(context.filesDir, BACKUP_FILENAME)
            FileOutputStream(internalFile).use { it.write(encryptedBytes) }

            val exportFile = File(context.cacheDir, "vyayah_backup_${System.currentTimeMillis()}.vyayah")
            FileOutputStream(exportFile).use { it.write(encryptedBytes) }

            exportFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreFromEncryptedBackup(
        context: Context,
        database: VyayahDatabase,
        vaultKey: String,
        sourceFile: File? = null
    ): Boolean {
        return try {
            val fileToRead = sourceFile ?: File(context.filesDir, BACKUP_FILENAME)
            if (!fileToRead.exists() || fileToRead.length() <= 28) return false

            val encryptedBytes = fileToRead.readBytes()
            val decryptedBytes = decryptWithVaultKey(vaultKey, encryptedBytes)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val payload = jsonHelper.decodeFromString<VaultBackupPayload>(jsonString)

            if (payload.accounts.isNotEmpty()) {
                database.accountDao().clearAll()
                database.accountDao().insertAll(payload.accounts)
            }
            if (payload.transactions.isNotEmpty()) {
                database.transactionDao().clearAll()
                database.transactionDao().insertAll(payload.transactions)
            }
            if (payload.categories.isNotEmpty()) {
                database.categoryDao().insertAll(payload.categories)
            }
            if (payload.budgets.isNotEmpty()) {
                database.budgetDao().clearAll()
                database.budgetDao().insertAll(payload.budgets)
            }
            if (payload.goals.isNotEmpty()) {
                database.goalDao().clearAll()
                database.goalDao().insertAll(payload.goals)
            }

            importVaultKey(context, vaultKey)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
