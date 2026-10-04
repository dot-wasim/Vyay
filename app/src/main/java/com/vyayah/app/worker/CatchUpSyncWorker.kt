package com.vyayah.app.worker

import android.content.Context
import android.provider.Telephony
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.data.model.SyncState
import com.vyayah.app.parser.SmsFilter

class CatchUpSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = VyayahDatabase.getInstance(applicationContext)
        val syncDao = database.syncDao()
        val syncState = syncDao.getSyncState() ?: SyncState()

        val contentResolver = applicationContext.contentResolver
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(syncState.lastProcessedTimestamp.toString())
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        var latestDate = syncState.lastProcessedTimestamp
        var latestId = syncState.lastProcessedSmsId

        try {
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val smsId = cursor.getLong(idIndex)
                    val sender = cursor.getString(addressIndex) ?: ""
                    val body = cursor.getString(bodyIndex) ?: ""
                    val date = cursor.getLong(dateIndex)

                    if (SmsFilter.shouldProcess(sender, body)) {
                        val workData = workDataOf(
                            SmsProcessWorker.KEY_SENDER to sender,
                            SmsProcessWorker.KEY_BODY to body,
                            SmsProcessWorker.KEY_TIMESTAMP to date
                        )
                        val request = OneTimeWorkRequestBuilder<SmsProcessWorker>()
                            .setInputData(workData)
                            .build()
                        WorkManager.getInstance(applicationContext).enqueue(request)
                    }

                    if (date > latestDate) {
                        latestDate = date
                        latestId = smsId
                    }
                }
            }

            syncDao.upsertSyncState(
                syncState.copy(
                    lastProcessedSmsId = latestId,
                    lastProcessedTimestamp = latestDate
                )
            )
            return Result.success()
        } catch (e: SecurityException) {
            // READ_SMS permission not granted yet
            return Result.retry()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    companion object {
        fun triggerOneTimeSync(context: Context, forceFullScan: Boolean = false) {
            val request = OneTimeWorkRequestBuilder<CatchUpSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
