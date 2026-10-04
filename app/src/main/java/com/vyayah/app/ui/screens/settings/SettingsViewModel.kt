package com.vyayah.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vyayah.app.data.local.RuleDao
import com.vyayah.app.data.local.SyncDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.data.model.SenderRule
import com.vyayah.app.data.model.SyncState
import com.vyayah.app.data.security.BackupKeyManager
import com.vyayah.app.worker.CatchUpSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsViewModel(
    private val ruleDao: RuleDao,
    private val syncDao: SyncDao,
    private val transactionDao: TransactionDao,
    private val database: VyayahDatabase
) : ViewModel() {

    val senderRules: StateFlow<List<SenderRule>> = ruleDao.getAllSenderRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Retrieves or generates the user's sovereign 12-word recovery key.
     * Named "Ledger Key" (Kosh Key).
     */
    fun getLedgerKey(context: Context): String {
        return BackupKeyManager.getOrCreateVaultKey(context)
    }

    /**
     * Exports an encrypted database backup file (.vyayah) encrypted with the Ledger Key.
     */
    suspend fun exportEncryptedBackup(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath("vyayah_encrypted.db")
            if (!dbPath.exists()) return@withContext null

            val rawBytes = dbPath.readBytes()
            val ledgerKey = getLedgerKey(context)
            val encryptedBackup = BackupKeyManager.encryptWithVaultKey(ledgerKey, rawBytes)

            val exportFile = File(context.cacheDir, "vyayah_backup_${System.currentTimeMillis()}.vyayah")
            FileOutputStream(exportFile).use { it.write(encryptedBackup) }
            exportFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Restores an encrypted database backup file using the user-provided Ledger Key.
     */
    suspend fun restoreEncryptedBackup(context: Context, backupFile: File, inputKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encryptedBytes = backupFile.readBytes()
            val decryptedDb = BackupKeyManager.decryptWithVaultKey(inputKey.trim(), encryptedBytes)

            val dbPath = context.getDatabasePath("vyayah_encrypted.db")
            FileOutputStream(dbPath).use { it.write(decryptedDb) }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun triggerBackfill(context: Context, monthsBack: Int) {
        viewModelScope.launch {
            val millisBack = monthsBack * 30L * 24 * 60 * 60 * 1000
            val startTime = System.currentTimeMillis() - millisBack
            val syncState = syncDao.getSyncState() ?: SyncState()
            syncDao.upsertSyncState(
                syncState.copy(
                    lastProcessedTimestamp = startTime,
                    lastBackfillAt = System.currentTimeMillis()
                )
            )

            val request = OneTimeWorkRequestBuilder<CatchUpSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    fun addSenderRule(pattern: String, bankName: String) {
        viewModelScope.launch {
            ruleDao.insertSenderRule(
                SenderRule(
                    senderPattern = pattern.trim(),
                    allowed = true,
                    bankName = bankName.ifBlank { null }
                )
            )
        }
    }

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
