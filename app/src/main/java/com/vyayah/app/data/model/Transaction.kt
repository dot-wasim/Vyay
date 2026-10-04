package com.vyayah.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class TransactionDirection {
    DEBIT,
    CREDIT
}

enum class TransactionType {
    PURCHASE,
    REFUND,
    REVERSAL,
    FAILED,
    ATM,
    TRANSFER,
    INCOME,
    BILL_PAYMENT,
    OTHER
}

enum class PaymentInstrument {
    CARD,
    UPI,
    NETBANKING,
    CASH,
    UNKNOWN
}

enum class TransactionStatus {
    CONFIRMED,
    NEEDS_REVIEW,
    IGNORED
}

enum class ParseSource {
    REGEX,
    AI,
    MANUAL
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["smsHash"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["upiRef"]),
        Index(value = ["status"])
    ]
)
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsHash: String,
    val sender: String,
    val rawBody: String?,
    val timestamp: Long,
    val amountMinor: Long, // Integer paise (e.g. 45000 = ₹450.00)
    val currency: String = "INR",
    val originalAmount: String? = null,
    val direction: TransactionDirection,
    val type: TransactionType,
    val instrument: PaymentInstrument = PaymentInstrument.UNKNOWN,
    val accountId: Long? = null,
    val merchantRaw: String? = null,
    val merchantNorm: String? = null,
    val upiVpa: String? = null,
    val upiRef: String? = null,
    val categoryId: Long? = null,
    val linkedTxnId: Long? = null, // for refunds and reversals
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val source: ParseSource = ParseSource.REGEX,
    val confidence: Float = 1.0f,
    val notes: String? = null
)
