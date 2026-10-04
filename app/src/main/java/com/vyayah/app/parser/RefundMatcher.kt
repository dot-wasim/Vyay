package com.vyayah.app.parser

import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Transaction
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.data.model.TransactionType

object RefundMatcher {

    const val REFUND_WINDOW_MILLIS = 60L * 24 * 60 * 60 * 1000 // 60 days
    const val REVERSAL_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000  // 7 days
    const val DUPLICATE_WINDOW_MILLIS = 5L * 60 * 1000          // 5 minutes

    /**
     * Checks if a debit and reversal match, allowing them to net to zero.
     */
    suspend fun findMatchingReversal(
        upiRef: String?,
        amountMinor: Long,
        timestamp: Long,
        transactionDao: TransactionDao
    ): Transaction? {
        if (!upiRef.isNullOrBlank()) {
            val existing = transactionDao.getByUpiRef(upiRef)
            if (existing != null && existing.direction == TransactionDirection.DEBIT) {
                return existing
            }
        }

        val candidates = transactionDao.findPotentialDebitMatchesForRefund(
            refundAmountMinor = amountMinor,
            refundTime = timestamp,
            windowMillis = REVERSAL_WINDOW_MILLIS
        )
        return candidates.firstOrNull { it.amountMinor == amountMinor }
    }

    /**
     * Attempts to find a matching prior purchase for a refund within 60 days.
     */
    suspend fun findMatchingPurchase(
        merchantNorm: String?,
        amountMinor: Long,
        timestamp: Long,
        transactionDao: TransactionDao
    ): Transaction? {
        val candidates = transactionDao.findPotentialDebitMatchesForRefund(
            refundAmountMinor = amountMinor,
            refundTime = timestamp,
            windowMillis = REFUND_WINDOW_MILLIS
        )

        // 1. Exact amount and merchant match
        if (!merchantNorm.isNullOrBlank()) {
            val exactMerchantMatch = candidates.firstOrNull {
                it.merchantNorm.equals(merchantNorm, ignoreCase = true)
            }
            if (exactMerchantMatch != null) return exactMerchantMatch
        }

        // 2. Amount match within window
        return candidates.firstOrNull { it.amountMinor >= amountMinor }
    }
}
