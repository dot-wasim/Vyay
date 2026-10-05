package com.vyayah.app.parser

import com.vyayah.app.data.model.PaymentInstrument
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.data.model.TransactionType

data class ParsedSmsResult(
    val amountMinor: Long,
    val currency: String = "INR",
    val direction: TransactionDirection,
    val type: TransactionType,
    val instrument: PaymentInstrument,
    val accountLast4: String?,
    val bankName: String,
    val merchantRaw: String?,
    val upiVpa: String?,
    val upiRef: String?,
    val availableBalanceMinor: Long? = null,
    val confidence: Float = 0.95f
)

object BankSmsParser {

    private val UPI_REF_REGEX = Regex("(?:UPI\\s+Ref(?:\\s+no\\.?)?|Ref(?:\\s+no\\.?)?|RRN|UTR|Txn(?:\\s+Id)?|IMPS\\s+Ref)[:\\s]*([A-Za-z0-9]{6,16})", RegexOption.IGNORE_CASE)
    private val VPA_REGEX = Regex("([a-zA-Z0-9.\\-_]+@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE)
    private val AVL_BAL_REGEX = Regex("(?:Avl(?:\\.|\\s+)?Bal(?:ance)?|Bal(?:ance)?)\\s*(?:is|:)?\\s*(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)", RegexOption.IGNORE_CASE)

    // Bank account or card last 4 digits
    private val LAST4_REGEX = Regex("(?:a/c(?: no)?|acct(?: no)?|account(?: no)?|card|ending with|ending in)\\s*(?:xx|x+|\\*+)?\\s*([0-9]{3,4})", RegexOption.IGNORE_CASE)

    fun parse(sender: String, body: String): ParsedSmsResult? {
        val upperSender = sender.uppercase()
        val upperBody = body.uppercase()
        val lowerBody = body.lowercase()

        // Stage 1a: Bank-Specific Template Matching (Highest Precision)
        val templateResult = when {
            upperSender.contains("HDFC") || upperBody.contains("HDFC") -> BankTemplates.parseHdfc(sender, body)
            upperSender.contains("SBI") || upperBody.contains("STATE BANK") -> BankTemplates.parseSbi(sender, body)
            upperSender.contains("ICICI") || upperBody.contains("ICICI") -> BankTemplates.parseIcici(sender, body)
            upperSender.contains("AXIS") || upperBody.contains("AXIS") -> BankTemplates.parseAxis(sender, body)
            upperSender.contains("PAYTM") || upperBody.contains("PAYTM") -> BankTemplates.parsePaytm(sender, body)
            else -> null
        }
        if (templateResult != null) return templateResult

        // Stage 1b: General Regex Fallback
        // 1. Determine Bank Name
        val bankName = detectBank(upperSender, body)


        // 2. Extract UPI Reference and VPA if present
        val upiRef = UPI_REF_REGEX.find(body)?.groupValues?.get(1)
        val upiVpa = VPA_REGEX.find(body)?.groupValues?.get(1)

        // 3. Extract Available Balance if present
        val avlBalMinor = AVL_BAL_REGEX.find(body)?.groupValues?.get(1)?.let {
            AmountParser.parseToMinorUnits(it)
        }

        // 4. Extract Account / Card Last 4
        val last4 = LAST4_REGEX.find(body)?.groupValues?.get(1)

        // 5. Determine Instrument
        val instrument = when {
            body.contains("UPI", ignoreCase = true) || upiVpa != null || upiRef != null -> PaymentInstrument.UPI
            body.contains("credit card", ignoreCase = true) || body.contains("card ending", ignoreCase = true) -> PaymentInstrument.CARD
            body.contains("ATM", ignoreCase = true) || body.contains("withdrawn", ignoreCase = true) -> PaymentInstrument.CASH
            body.contains("netbanking", ignoreCase = true) || body.contains("NEFT", ignoreCase = true) || body.contains("IMPS", ignoreCase = true) -> PaymentInstrument.NETBANKING
            else -> PaymentInstrument.UNKNOWN
        }

        // 6. Determine Direction and TransactionType
        val isBillPayment = lowerBody.contains("bill payment") || (lowerBody.contains("payment") && lowerBody.contains("card") && lowerBody.contains("received"))
        val isRefund = lowerBody.contains("refund")
        val isReversal = lowerBody.contains("reversed") || lowerBody.contains("reversal")
        val isFailed = lowerBody.contains("failed") || lowerBody.contains("declined")
        val isSalary = lowerBody.contains("salary") || (lowerBody.contains("credited") && (lowerBody.contains("payroll") || lowerBody.contains("employer")))
        val isAtm = lowerBody.contains("atm") || (lowerBody.contains("cash") && lowerBody.contains("withdrawn"))
        val isCredit = lowerBody.contains("credited") || lowerBody.contains("deposited") || lowerBody.contains("received") || lowerBody.contains("cashback")
        val isDebit = lowerBody.contains("debited") || lowerBody.contains("spent") || lowerBody.contains("paid") || lowerBody.contains("withdrawn") || lowerBody.contains("sent") || lowerBody.contains("transferred") || lowerBody.contains("deducted") || lowerBody.contains("transfer of")

        val (direction, type) = when {
            isBillPayment -> Pair(TransactionDirection.DEBIT, TransactionType.BILL_PAYMENT)
            isRefund -> Pair(TransactionDirection.CREDIT, TransactionType.REFUND)
            isReversal -> Pair(TransactionDirection.CREDIT, TransactionType.REVERSAL)
            isFailed -> Pair(TransactionDirection.DEBIT, TransactionType.FAILED)
            isSalary -> Pair(TransactionDirection.CREDIT, TransactionType.INCOME)
            isAtm -> Pair(TransactionDirection.DEBIT, TransactionType.ATM)
            isCredit -> Pair(TransactionDirection.CREDIT, TransactionType.INCOME)
            isDebit -> Pair(TransactionDirection.DEBIT, TransactionType.PURCHASE)
            else -> return null // Unable to confidently determine transaction intent
        }

        // 7. Extract Main Transaction Amount
        // Exclude Avl Bal match when extracting transaction amount
        val bodyWithoutAvlBal = body.replace(AVL_BAL_REGEX, "")
        val (amountMinor, _) = AmountParser.extractTransactionAmount(bodyWithoutAvlBal)
            ?: AmountParser.extractTransactionAmount(body)
            ?: return null

        // 8. Extract Merchant
        val rawMerchant = extractMerchant(body, type)

        return ParsedSmsResult(
            amountMinor = amountMinor,
            currency = "INR",
            direction = direction,
            type = type,
            instrument = instrument,
            accountLast4 = last4,
            bankName = bankName,
            merchantRaw = rawMerchant,
            upiVpa = upiVpa,
            upiRef = upiRef,
            availableBalanceMinor = avlBalMinor,
            confidence = 0.95f
        )
    }

    private fun detectBank(sender: String, body: String): String {
        val s = sender.uppercase()
        val b = body.uppercase()
        return when {
            s.contains("HDFC") || b.contains("HDFC") -> "HDFC Bank"
            s.contains("SBI") || b.contains("STATE BANK OF INDIA") -> "SBI"
            s.contains("ICICI") || b.contains("ICICI") -> "ICICI Bank"
            s.contains("AXIS") || b.contains("AXIS") -> "Axis Bank"
            s.contains("KOTAK") || b.contains("KOTAK") -> "Kotak Mahindra Bank"
            s.contains("PNB") || b.contains("PUNJAB NATIONAL") -> "PNB"
            s.contains("IDFC") || b.contains("IDFC") -> "IDFC FIRST Bank"
            s.contains("BOB") || b.contains("BANK OF BARODA") -> "Bank of Baroda"
            s.contains("INDUS") || b.contains("INDUSIND") -> "IndusInd Bank"
            s.contains("CANARA") || b.contains("CANBNK") -> "Canara Bank"
            s.contains("UNION") || b.contains("UNION BANK") -> "Union Bank"
            s.contains("PAYTM") -> "Paytm Payments Bank"
            else -> "Bank"
        }
    }

    private val MERCHANT_EXTRACTORS = listOf(
        Regex("(?:at|to|info:)\\s+([A-Za-z0-9.\\-_* ]+?)(?:\\s+on|\\s+dt|\\s+dated|\\s+ref|\\s+avl|\\s+bal|\\s*\\.|\$)", RegexOption.IGNORE_CASE),
        Regex("(?:vpa\\s+)([A-Za-z0-9.\\-_@]+)", RegexOption.IGNORE_CASE)
    )

    private fun extractMerchant(body: String, type: TransactionType): String? {
        if (type == TransactionType.ATM) return "ATM Cash Withdrawal"
        if (type == TransactionType.BILL_PAYMENT) return "Credit Card Bill Payment"

        for (regex in MERCHANT_EXTRACTORS) {
            val match = regex.find(body)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.length in 2..50 && !candidate.contains("INR", ignoreCase = true) && !candidate.contains("Rs", ignoreCase = true)) {
                    return candidate
                }
            }
        }
        return null
    }
}
