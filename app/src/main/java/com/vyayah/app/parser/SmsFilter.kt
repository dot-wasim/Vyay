package com.vyayah.app.parser

object SmsFilter {

    private val TRANSACTION_KEYWORDS = listOf(
        "debited", "credited", "spent", "paid", "refund", "reversed",
        "upi", "a/c", "acct", "account", "card", "inr", "rs.", "rs ", "₹",
        "transferred", "withdrawn", "avl bal", "available balance", "txn"
    )

    // Strict hard exclusions: If ANY of these appear, it is NEVER a ledger transaction.
    // Even if sent by VM-SBIUPI or AD-HDFCBK, OTPs must NEVER enter the ledger or adjust balances.
    private val HARD_EXCLUSION_KEYWORDS = listOf(
        "otp", "one time password", "verification code", "secret code", "security code",
        "do not share", "never share", "valid for", "login alert", "logged in",
        "mandate otp", "auth code", "is your code", "use code", "secret otp",
        "not you? call", "pre-approved", "apply now", "loan offer", "congratulations! you are eligible",
        "hurry! offer", "claim your", "click here to", "exclusive offer"
    )

    private val PERSONAL_NUMBER_REGEX = Regex("^[+]?[0-9]{10,13}$")

    /**
     * Checks if the sender and body indicate an authentic transaction SMS.
     * Returns true if valid for parsing, false if ignored.
     */
    fun shouldProcess(sender: String, body: String, customAllowedSenders: List<String> = emptyList()): Boolean {
        val trimmedSender = sender.trim()
        val lowerBody = body.lowercase()

        // 1. Exclude regular phone numbers (friends, family, spam numbers)
        if (trimmedSender.matches(PERSONAL_NUMBER_REGEX)) {
            return false
        }

        // 2. CRITICAL GATE: Hard exclusions (OTPs, passwords, login alerts, promos)
        // Must run FIRST even if the sender is an authentic bank like VM-SBIUPI or AD-HDFCBK!
        if (HARD_EXCLUSION_KEYWORDS.any { lowerBody.contains(it) }) {
            return false
        }

        // 3. Keyword gate: Must contain financial transaction indicators
        val hasTransactionKeyword = TRANSACTION_KEYWORDS.any { lowerBody.contains(it) }
        if (!hasTransactionKeyword) {
            return false
        }

        // 4. Sender verification
        if (isKnownBankSender(trimmedSender) || customAllowedSenders.any { trimmedSender.contains(it, ignoreCase = true) }) {
            return true
        }

        // Fallback: If header matches standard Indian 2-letter operator + 6-character header pattern (e.g. BZ-SBIUPI, AD-HDFCBK)
        if (trimmedSender.contains("-") && (lowerBody.contains("debited") || lowerBody.contains("credited") || lowerBody.contains("spent"))) {
            return true
        }

        return false
    }

    fun isKnownBankSender(sender: String): Boolean {
        val s = sender.uppercase()
        val bankTokens = listOf(
            "HDFC", "SBI", "ICICI", "AXIS", "KOTAK", "PNB", "IDFC",
            "BOB", "INDUS", "CANARA", "UNION", "PAYTM", "CRED", "CITI",
            "YESBK", "RBL", "FEDERAL", "SCISMS", "AUFIN"
        )
        return bankTokens.any { s.contains(it) }
    }
}
