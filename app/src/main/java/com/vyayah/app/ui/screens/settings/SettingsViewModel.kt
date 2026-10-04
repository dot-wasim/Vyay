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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SettingsViewModel(
    private val ruleDao: RuleDao,
    private val syncDao: SyncDao,
    private val transactionDao: TransactionDao,
    private val database: VyayahDatabase
) : ViewModel() {

    private val _appLockEnabled = MutableStateFlow(false)
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _hideAmountsDefault = MutableStateFlow(true)
    val hideAmountsDefault: StateFlow<Boolean> = _hideAmountsDefault.asStateFlow()

    private val _flagSecureEnabled = MutableStateFlow(true)
    val flagSecureEnabled: StateFlow<Boolean> = _flagSecureEnabled.asStateFlow()

    val senderRules: StateFlow<List<SenderRule>> = ruleDao.getAllSenderRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAppLock(enabled: Boolean) {
        _appLockEnabled.value = enabled
    }

    fun toggleHideAmountsDefault(enabled: Boolean) {
        _hideAmountsDefault.value = enabled
    }

    fun toggleFlagSecure(enabled: Boolean) {
        _flagSecureEnabled.value = enabled
    }

    fun getLedgerKey(context: Context): String {
        return BackupKeyManager.getOrCreateVaultKey(context)
    }

    fun pasteAndImportLedgerKey(context: Context, keyPhrase: String): Boolean {
        return BackupKeyManager.importVaultKey(context, keyPhrase)
    }

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

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
