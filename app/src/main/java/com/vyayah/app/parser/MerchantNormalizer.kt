package com.vyayah.app.parser

object MerchantNormalizer {

    private val PREFIX_CLEANERS = listOf(
        Regex("^(?:POS\\s*\\d*\\s*)", RegexOption.IGNORE_CASE),
        Regex("^(?:E-?COM\\s*)", RegexOption.IGNORE_CASE),
        Regex("^(?:INF\\*|IPS\\*|PYTM\\*|BILLDESK\\*|RAZORPAY\\*)", RegexOption.IGNORE_CASE),
        Regex("^(?:AT\\s+|TO\\s+|INFO:\\s*)", RegexOption.IGNORE_CASE)
    )

    private val VPA_BRAND_MAP = mapOf(
        "swiggy" to "Swiggy",
        "zomato" to "Zomato",
        "blinkit" to "Blinkit",
        "zepto" to "Zepto",
        "bigbasket" to "BigBasket",
        "amazon" to "Amazon",
        "flipkart" to "Flipkart",
        "uber" to "Uber",
        "ola" to "Ola",
        "dunzo" to "Dunzo",
        "netflix" to "Netflix",
        "spotify" to "Spotify",
        "hotstar" to "Disney+ Hotstar",
        "makemytrip" to "MakeMyTrip",
        "irctc" to "IRCTC",
        "cred" to "CRED",
        "bookmyshow" to "BookMyShow"
    )

    fun normalize(rawMerchant: String?, vpa: String? = null): String {
        if (!vpa.isNullOrBlank()) {
            val vpaPrefix = vpa.substringBefore("@").lowercase()
            for ((key, brand) in VPA_BRAND_MAP) {
                if (vpaPrefix.contains(key)) return brand
            }
            // If VPA contains human name or store
            val cleanVpa = vpaPrefix.replace(".", " ").replace("_", " ").replace("-", " ")
                .replace(Regex("\\d+"), "").trim()
            if (cleanVpa.isNotBlank() && cleanVpa.length > 2) {
                return cleanVpa.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            }
        }

        if (rawMerchant.isNullOrBlank()) return "Unknown Merchant"

        var cleaned = rawMerchant.trim()
        for (regex in PREFIX_CLEANERS) {
            cleaned = cleaned.replace(regex, "").trim()
        }

        val lowerCleaned = cleaned.lowercase()
        for ((key, brand) in VPA_BRAND_MAP) {
            if (lowerCleaned.contains(key)) return brand
        }

        // Clean trailing city or terminal codes (e.g. "SWIGGY BANGALORE IN" -> "Swiggy")
        cleaned = cleaned.replace(Regex("\\s+(?:IN|IND|BANGALORE|MUMBAI|DELHI|HYDERABAD|PUNE|CHENNAI)$", RegexOption.IGNORE_CASE), "")

        return if (cleaned.isBlank()) "Merchant" else cleaned
    }
}
