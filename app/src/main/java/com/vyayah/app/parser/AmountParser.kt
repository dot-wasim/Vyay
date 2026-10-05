package com.vyayah.app.parser

object AmountParser {

    private val AMOUNT_REGEXES = listOf(
        // Currency prefix: "Rs. 1336.05", "INR 1000", "₹1,336.05", "Rs 1000.00"
        Regex(
            "(?:INR|Rs\\.?|₹|amount\\s+of\\s+(?:INR|Rs\\.?|₹)?)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)",
            RegexOption.IGNORE_CASE
        ),
        // Currency suffix: "1336.05 INR", "1000 Rs."
        Regex(
            "([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)\\s*(?:INR|Rs\\.?|₹)",
            RegexOption.IGNORE_CASE
        ),
        // Verb prefix: "debited by 1336.05", "paid 1000", "spent 450.50"
        Regex(
            "(?:debited\\s+(?:by|for|of)|credited\\s+(?:by|for|with)|paid|spent|transferred\\s+(?:by|of)?)\\s*(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)",
            RegexOption.IGNORE_CASE
        )
    )

    /**
     * Parses an amount string to integer paise.
     * Example: "1,234.50" -> 123450L
     * Example: "1336.05" -> 133605L
     * Example: "1000" -> 100000L
     */
    fun parseToMinorUnits(rawAmount: String): Long {
        val cleaned = rawAmount.replace(",", "").trim()
        val parts = cleaned.split(".")
        val rupees = parts[0].toLongOrNull() ?: 0L
        val paise = if (parts.size > 1) {
            val p = parts[1]
            when (p.length) {
                0 -> 0L
                1 -> (p + "0").toLongOrNull() ?: 0L
                else -> p.take(2).toLongOrNull() ?: 0L
            }
        } else {
            0L
        }
        return (rupees * 100) + paise
    }

    /**
     * Extracts the first transaction amount found in the text.
     * Returns Pair(amountInPaise, rawAmountString) or null if not found.
     */
    fun extractTransactionAmount(text: String): Pair<Long, String>? {
        for (regex in AMOUNT_REGEXES) {
            val match = regex.find(text)
            if (match != null) {
                val rawNum = match.groupValues[1]
                val paise = parseToMinorUnits(rawNum)
                if (paise > 0L) {
                    return Pair(paise, rawNum)
                }
            }
        }
        return null
    }

    /**
     * Formats integer paise into Indian Rupee presentation format.
     * Example: 12345600L -> "₹1,23,456"
     */
    fun formatPaiseToInr(paise: Long, includePaise: Boolean = (paise % 100 != 0L)): String {
        val isNegative = paise < 0
        val absPaise = kotlin.math.abs(paise)
        val rupees = absPaise / 100
        val p = absPaise % 100

        val rupeeString = formatIndianNumbering(rupees)
        val prefix = if (isNegative) "-₹" else "₹"
        return if ((includePaise || p > 0) && p > 0) {
            "$prefix$rupeeString.${p.toString().padStart(2, '0')}"
        } else {
            "$prefix$rupeeString"
        }
    }

    /**
     * Alias for formatting Indian Currency from paise minor units.
     */
    fun formatIndianCurrency(paise: Long, includePaise: Boolean = false): String =
        formatPaiseToInr(paise, includePaise)

    private fun formatIndianNumbering(n: Long): String {
        val s = n.toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        var remaining = s.substring(0, s.length - 3)
        val sb = java.lang.StringBuilder()
        while (remaining.length > 2) {
            sb.insert(0, "," + remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        return remaining + sb.toString() + "," + lastThree
    }
}
