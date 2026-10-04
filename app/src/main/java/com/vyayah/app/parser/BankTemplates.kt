package com.vyayah.app.parser

import com.vyayah.app.data.model.PaymentInstrument
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.data.model.TransactionType

data class BankTemplate(
    val bankIdentifier: String,
    val parseFunction: (sender: String, body: String) -> ParsedSmsResult?
)

object BankTemplates {

    private val AVL_BAL_REGEX = Regex(
        "(?:Avl(?:\\.|\\s+)?(?:Bal|Balance)|Available\\s+Balance|Total\\s+Bal|Bal(?:ance)?|Bal\\s+Limit|Available\\s+limit)\\s*(?:is|:)?\\s*(?:INR|Rs\\.?|₹)?\\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    private val UPI_REF_REGEX = Regex(
        "(?:UPI\\s+Ref(?:\\s+no)?|Ref(?:\\s+no)?|UTR|rrn)[:\\s]*([0-9]{8,14})",
        RegexOption.IGNORE_CASE
    )

    private val VPA_REGEX = Regex(
        "([a-zA-Z0-9.\\-_]+@[a-zA-Z0-9]+)",
        RegexOption.IGNORE_CASE
    )

    private val LAST4_REGEX = Regex(
        "(?:a/c(?:\\s+no)?|acct(?:\\s+no)?|account(?:\\s+no)?|card(?:\\s+no)?|ending\\s+with|ending\\s+in)\\s*(?:xx|x+|\\*+)?\\s*([0-9]{3,4})",
        RegexOption.IGNORE_CASE
    )

    /**
     * HDFC Bank specialized template matcher
     */
    fun parseHdfc(sender: String, body: String): ParsedSmsResult? {
        val s = sender.uppercase()
        val b = body.uppercase()
        if (!s.contains("HDFC") && !b.contains("HDFC")) return null

        val isCreditCard = b.contains("HDFC BANK CARD") || b.contains("CREDIT CARD")
        val isUpi = b.contains("UPI") || VPA_REGEX.containsMatchIn(body)

        val instrument = when {
            isCreditCard -> PaymentInstrument.CARD
            isUpi -> PaymentInstrument.UPI
            b.contains("ATM") -> PaymentInstrument.CASH
            b.contains("NETBANKING") || b.contains("NEFT") || b.contains("IMPS") -> PaymentInstrument.NETBANKING
            else -> PaymentInstrument.UNKNOWN
        }

        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val upiVpa = VPA_REGEX.find(body)?.groupValues?.get(1)
        val avlBal = extractAvlBal(body)

        val direction: TransactionDirection
        val type: TransactionType
        when {
            b.contains("REFUND") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.REFUND
            }
            b.contains("REVERSED") || b.contains("REVERSAL") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.REVERSAL
            }
            b.contains("SALARY") || (b.contains("CREDITED") && b.contains("PAYROLL")) -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.INCOME
            }
            b.contains("CREDITED") || b.contains("DEPOSITED") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.INCOME
            }
            b.contains("WITHDRAWN") && b.contains("ATM") -> {
                direction = TransactionDirection.DEBIT
                type = TransactionType.ATM
            }
            b.contains("DEBITED") || b.contains("SPENT") || b.contains("PAID") -> {
                direction = TransactionDirection.DEBIT
                type = TransactionType.PURCHASE
            }
            else -> return null
        }

        val amountMinor = extractCleanAmount(body) ?: return null
        val merchant = extractHdfcMerchant(body, type)

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = instrument,
            accountLast4 = last4,
            bankName = "HDFC Bank",
            merchantRaw = merchant,
            upiVpa = upiVpa,
            upiRef = upiRef,
            availableBalanceMinor = avlBal,
            confidence = 0.98f
        )
    }

    /**
     * SBI (State Bank of India) specialized template matcher
     */
    fun parseSbi(sender: String, body: String): ParsedSmsResult? {
        val s = sender.uppercase()
        val b = body.uppercase()
        if (!s.contains("SBI") && !b.contains("STATE BANK OF INDIA")) return null

        val isUpi = s.contains("SBIUPI") || b.contains("UPI") || VPA_REGEX.containsMatchIn(body)
        val instrument = when {
            isUpi -> PaymentInstrument.UPI
            b.contains("CARD") -> PaymentInstrument.CARD
            b.contains("ATM") -> PaymentInstrument.CASH
            else -> PaymentInstrument.UNKNOWN
        }

        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val upiVpa = VPA_REGEX.find(body)?.groupValues?.get(1)
        val avlBal = extractAvlBal(body)

        val direction: TransactionDirection
        val type: TransactionType
        when {
            b.contains("REFUND") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.REFUND
            }
            b.contains("REVERSED") || b.contains("REVERSAL") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.REVERSAL
            }
            b.contains("CREDITED") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.INCOME
            }
            b.contains("ATM") && b.contains("WITHDRAWN") -> {
                direction = TransactionDirection.DEBIT
                type = TransactionType.ATM
            }
            b.contains("DEBITED") || b.contains("TRANSFERRED TO") || b.contains("PAID") -> {
                direction = TransactionDirection.DEBIT
                type = TransactionType.PURCHASE
            }
            else -> return null
        }

        val amountMinor = extractCleanAmount(body) ?: return null
        val merchant = extractSbiMerchant(body, type)

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = instrument,
            accountLast4 = last4,
            bankName = "SBI",
            merchantRaw = merchant,
            upiVpa = upiVpa,
            upiRef = upiRef,
            availableBalanceMinor = avlBal,
            confidence = 0.98f
        )
    }

    /**
     * ICICI Bank specialized template matcher
     */
    fun parseIcici(sender: String, body: String): ParsedSmsResult? {
        val s = sender.uppercase()
        val b = body.uppercase()
        if (!s.contains("ICICI")) return null

        val isCard = b.contains("CREDIT CARD") || b.contains("DEBIT CARD")
        val isUpi = b.contains("UPI") || VPA_REGEX.containsMatchIn(body)

        val instrument = when {
            isCard -> PaymentInstrument.CARD
            isUpi -> PaymentInstrument.UPI
            b.contains("ATM") -> PaymentInstrument.CASH
            else -> PaymentInstrument.UNKNOWN
        }

        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val upiVpa = VPA_REGEX.find(body)?.groupValues?.get(1)
        val avlBal = extractAvlBal(body)

        val direction: TransactionDirection
        val type: TransactionType
        when {
            b.contains("REFUND") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.REFUND
            }
            b.contains("CREDITED") -> {
                direction = TransactionDirection.CREDIT
                type = TransactionType.INCOME
            }
            b.contains("DEBITED") || b.contains("USED FOR A TRANSACTION") || b.contains("PAID") -> {
                direction = TransactionDirection.DEBIT
                type = TransactionType.PURCHASE
            }
            else -> return null
        }

        val amountMinor = extractCleanAmount(body) ?: return null

        // Merchant extraction for ICICI often looks like "info: Swiggy" or "at AMAZON INDIA"
        val merchant = Regex("(?:info:\\s*|at\\s+|to\\s+)([A-Za-z0-9.\\-_* ]+?)(?:\\.\\s|\\s+on|\\s+dt|;|\$)", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = instrument,
            accountLast4 = last4,
            bankName = "ICICI Bank",
            merchantRaw = merchant,
            upiVpa = upiVpa,
            upiRef = upiRef,
            availableBalanceMinor = avlBal,
            confidence = 0.98f
        )
    }

    /**
     * Axis Bank specialized template matcher
     */
    fun parseAxis(sender: String, body: String): ParsedSmsResult? {
        val s = sender.uppercase()
        val b = body.uppercase()
        if (!s.contains("AXIS")) return null

        val isCard = b.contains("CARD NO") || b.contains("CREDIT CARD")
        val isUpi = b.contains("UPI") || VPA_REGEX.containsMatchIn(body)

        val instrument = when {
            isCard -> PaymentInstrument.CARD
            isUpi -> PaymentInstrument.UPI
            b.contains("ATM") -> PaymentInstrument.CASH
            else -> PaymentInstrument.UNKNOWN
        }

        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val upiVpa = VPA_REGEX.find(body)?.groupValues?.get(1)
        val avlBal = extractAvlBal(body)

        val direction = if (b.contains("CREDITED") || b.contains("REFUND")) TransactionDirection.CREDIT else TransactionDirection.DEBIT
        val type = when {
            b.contains("REFUND") -> TransactionType.REFUND
            b.contains("CREDITED") -> TransactionType.INCOME
            else -> TransactionType.PURCHASE
        }

        val amountMinor = extractCleanAmount(body) ?: return null

        // Merchant in Axis: "towards UBER INDIA" or "at BOOKMYSHOW" or "Payee: XYZ"
        val merchant = Regex("(?:towards\\s+|at\\s+|Payee:\\s*)([A-Za-z0-9.\\-_* ]+?)(?:\\.\\s|\\s+on|\\s+dt|\\s*\\(|\$)", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = instrument,
            accountLast4 = last4,
            bankName = "Axis Bank",
            merchantRaw = merchant,
            upiVpa = upiVpa,
            upiRef = upiRef,
            availableBalanceMinor = avlBal,
            confidence = 0.98f
        )
    }

    /**
     * Paytm Payments Bank specialized template matcher
     */
    fun parsePaytm(sender: String, body: String): ParsedSmsResult? {
        val s = sender.uppercase()
        val b = body.uppercase()
        if (!s.contains("PAYTM")) return null

        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val avlBal = extractAvlBal(body)

        val direction = if (b.contains("RECEIVED") || b.contains("ADDED")) TransactionDirection.CREDIT else TransactionDirection.DEBIT
        val type = if (direction == TransactionDirection.CREDIT) TransactionType.INCOME else TransactionType.PURCHASE

        val amountMinor = extractCleanAmount(body) ?: return null

        // "Paid Rs. 45 to Sharma Kirana (UPI Ref...)"
        val merchant = Regex("(?:Paid(?:\\s+INR|\\s+Rs\\.?|\\s+₹)?\\s*[0-9.,]+\\s+to\\s+)([A-Za-z0-9.\\-_* ]+?)(?:\\s*\\(|\$|\\s+on)", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = PaymentInstrument.UPI,
            accountLast4 = last4,
            bankName = "Paytm Payments Bank",
            merchantRaw = merchant,
            upiVpa = null,
            upiRef = upiRef,
            availableBalanceMinor = avlBal,
            confidence = 0.98f
        )
    }

    // Helper functions
    private fun extractCleanAmount(body: String): Long? {
        val bodyWithoutAvlBal = body.replace(AVL_BAL_REGEX, "")
        return AmountParser.extractTransactionAmount(bodyWithoutAvlBal)?.first
            ?: AmountParser.extractTransactionAmount(body)?.first
    }

    private fun extractAvlBal(body: String): Long? {
        return AVL_BAL_REGEX.find(body)?.groupValues?.get(1)?.let {
            AmountParser.parseToMinorUnits(it)
        }
    }

    private fun extractHdfcMerchant(body: String, type: TransactionType): String? {
        if (type == TransactionType.ATM) return "ATM Cash Withdrawal"
        val regex = Regex("(?:at\\s+|to\\s+)([A-Za-z0-9.\\-_* ]+?)(?:\\s+on|\\s+dt|\\s*\\.|\$)", RegexOption.IGNORE_CASE)
        return regex.find(body)?.groupValues?.get(1)?.trim()?.takeIf { !it.contains("VPA", true) }
    }

    private fun extractSbiMerchant(body: String, type: TransactionType): String? {
        if (type == TransactionType.ATM) return "SBI ATM Cash"
        val regex = Regex("(?:at\\s+|transfer(?:red)?\\s+to\\s+VPA\\s+|transferred\\s+to\\s+)([A-Za-z0-9.\\-_*@ ]+?)(?:\\s*\\(|\\s*\\.|\\s+on|\$)", RegexOption.IGNORE_CASE)
        return regex.find(body)?.groupValues?.get(1)?.trim()
    }
}
