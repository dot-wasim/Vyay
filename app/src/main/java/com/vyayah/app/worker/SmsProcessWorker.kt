package com.vyayah.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vyayah.app.ai.CategorizationEngine
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.data.model.*
import com.vyayah.app.parser.BankSmsParser
import com.vyayah.app.parser.MerchantNormalizer
import com.vyayah.app.parser.RefundMatcher
import java.security.MessageDigest

class SmsProcessWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_SENDER = "sms_sender"
        const val KEY_BODY = "sms_body"
        const val KEY_TIMESTAMP = "sms_timestamp"

        fun computeSmsHash(sender: String, body: String, timestamp: Long): String {
            val raw = "$sender|$body|$timestamp"
            val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val database = VyayahDatabase.getInstance(applicationContext)
        val transactionDao = database.transactionDao()
        val accountDao = database.accountDao()
        val categoryDao = database.categoryDao()
        val ruleDao = database.ruleDao()
        val categorizationEngine = CategorizationEngine(categoryDao, ruleDao)

        // 1. Deduplication check
        val smsHash = computeSmsHash(sender, body, timestamp)
        val existingTxn = transactionDao.getBySmsHash(smsHash)
        if (existingTxn != null) {
            return Result.success()
        }

        // 2. Parse SMS
        val parsed = BankSmsParser.parse(sender, body) ?: return Result.success()

        // 3. Normalize Merchant
        val merchantNorm = MerchantNormalizer.normalize(parsed.merchantRaw, parsed.upiVpa)

        // 4. Categorize
        val categoryId = categorizationEngine.categorize(merchantNorm, body)

        // 5. Account matching / creation
        var accountId: Long? = null
        if (!parsed.accountLast4.isNullOrBlank()) {
            val account = accountDao.getByBankAndLast4(parsed.bankName, parsed.accountLast4)
                ?: accountDao.getByLast4(parsed.accountLast4)

            if (account != null) {
                accountId = account.id
                // Balance calculation:
                // If SMS specifies Avl Bal, use bank's official number
                val newBalance = parsed.availableBalanceMinor ?: when (parsed.direction) {
                    TransactionDirection.DEBIT -> account.currentBalance - parsed.amountMinor
                    TransactionDirection.CREDIT -> account.currentBalance + parsed.amountMinor
                }
                accountDao.updateBalance(account.id, newBalance, timestamp)
            } else {
                // Auto-suggest / create initial account from SMS
                val initialBalance = parsed.availableBalanceMinor ?: 0L
                val newAcc = Account(
                    bank = parsed.bankName,
                    type = if (parsed.instrument == PaymentInstrument.CARD) AccountType.CREDIT else AccountType.SAVINGS,
                    last4 = parsed.accountLast4,
                    nickname = "${parsed.bankName} ${parsed.accountLast4}",
                    openingBalance = initialBalance,
                    currentBalance = initialBalance,
                    lastSmsBalance = parsed.availableBalanceMinor,
                    lastReconciledAt = timestamp
                )
                accountId = accountDao.insert(newAcc)
            }
        }

        // 6. Check for Refund or Reversal match
        var linkedTxnId: Long? = null
        if (parsed.type == TransactionType.REFUND) {
            val matchingDebit = RefundMatcher.findMatchingPurchase(merchantNorm, parsed.amountMinor, timestamp, transactionDao)
            linkedTxnId = matchingDebit?.id
        } else if (parsed.type == TransactionType.REVERSAL) {
            val matchingDebit = RefundMatcher.findMatchingReversal(parsed.upiRef, parsed.amountMinor, timestamp, transactionDao)
            linkedTxnId = matchingDebit?.id
        }

        // 7. Store Transaction
        val transaction = Transaction(
            smsHash = smsHash,
            sender = sender,
            rawBody = body,
            timestamp = timestamp,
            amountMinor = parsed.amountMinor,
            currency = parsed.currency,
            direction = parsed.direction,
            type = parsed.type,
            instrument = parsed.instrument,
            accountId = accountId,
            merchantRaw = parsed.merchantRaw,
            merchantNorm = merchantNorm,
            upiVpa = parsed.upiVpa,
            upiRef = parsed.upiRef,
            categoryId = categoryId,
            linkedTxnId = linkedTxnId,
            status = TransactionStatus.CONFIRMED,
            source = ParseSource.REGEX,
            confidence = parsed.confidence
        )

        transactionDao.insert(transaction)
        return Result.success()
    }
}
