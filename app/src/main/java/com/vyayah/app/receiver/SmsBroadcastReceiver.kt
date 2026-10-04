package com.vyayah.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.vyayah.app.parser.SmsFilter
import com.vyayah.app.worker.SmsProcessWorker

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: return
        val bodyBuilder = StringBuilder()
        for (msg in messages) {
            bodyBuilder.append(msg.displayMessageBody ?: "")
        }
        val fullBody = bodyBuilder.toString()
        val timestamp = messages[0].timestampMillis

        // Quick privacy & keyword filter check before waking up WorkManager
        if (!SmsFilter.shouldProcess(sender, fullBody)) {
            return
        }

        // Enqueue WorkManager for database writing, balance updating and AI fallbacks
        val workData = workDataOf(
            SmsProcessWorker.KEY_SENDER to sender,
            SmsProcessWorker.KEY_BODY to fullBody,
            SmsProcessWorker.KEY_TIMESTAMP to timestamp
        )

        val request = OneTimeWorkRequestBuilder<SmsProcessWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
