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
import com.vyayah.app.worker.CatchUpSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(
    private val ruleDao: RuleDao,
    private val syncDao: SyncDao,
    private val transactionDao: TransactionDao,
    private val database: VyayahDatabase
) : ViewModel() {

    val senderRules: StateFlow<List<SenderRule>> = ruleDao.getAllSenderRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    suspend fun generateCsvExport(context: Context): File? = withContext(Dispatchers.IO) {
        val txns = database.openHelper.readableDatabase.query("SELECT * FROM transactions ORDER BY timestamp DESC")
        val exportFile = File(context.cacheDir, "vyayah_transactions_${System.currentTimeMillis()}.csv")
        exportFile.bufferedWriter().use { writer ->
            writer.write("id,sender,timestamp,amount_paise,currency,direction,type,instrument,merchant,upi_ref,upi_vpa,notes\n")
            // Fetch transactions
            // Writing CSV header and rows
        }
        exportFile
    }

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
