package com.vyayah.app.parser

object MerchantNormalizer {

    private val PREFIX_CLEANERS = listOf(
        Regex("^(?:POS\\s*\\d*\\s*)", RegexOption.IGNORE_CASE),
        Regex("^(?:E-?COM\\s*)", RegexOption.IGNORE_CASE),
        Regex("^(?:INF\\*|IPS\\*|PYTM\\*|BILLDESK\\*|RAZORPAY\\*)", RegexOption.IGNORE_CASE),
        Regex("^(?:AT\\s+|TO\\s+|INFO:\\s*)", RegexOption.IGNORE_CASE)
    )

    private val VPA_BRAND_MAP = mapOf(
        // Food & Dining (Fun Mode)
        "swiggy" to "Swiggy 🍕",
        "zomato" to "Zomato 🍔",
        "mcdonalds" to "McDonald's 🍟",
        "mcd" to "McDonald's 🍟",
        "dominos" to "Domino's Pizza 🍕",
        "starbucks" to "Starbucks ☕",
        "kfc" to "KFC 🍗",
        "burger king" to "Burger King 🍔",
        "chai" to "Chai Point ☕",

        // Groceries
        "blinkit" to "Blinkit 🛒",
        "zepto" to "Zepto ⚡",
        "bigbasket" to "BigBasket 🥦",
        "instamart" to "Swiggy Instamart 🛒",
        "dmart" to "DMart 🛒",
        "dunzo" to "Dunzo 🛵",

        // Entertainment (Fun Mode)
        "bookmyshow" to "BookMyShow 🎟️",
        "bms" to "BookMyShow 🎟️",
        "pvr" to "PVR Cinemas 🍿",
        "inox" to "INOX Cinemas 🎬",
        "netflix" to "Netflix 🍿",
        "spotify" to "Spotify 🎧",
        "hotstar" to "Disney+ Hotstar 📺",
        "prime video" to "Prime Video 🎬",
        "steam" to "Steam Games 🎮",
        "playstation" to "PlayStation 🎮",

        // Travel & Transport
        "indian rail" to "Indian Railway 🚂",
        "railway" to "Indian Railway 🚂",
        "irctc" to "Indian Railway 🚂",
        "uber" to "Uber 🚗",
        "ola" to "Ola 🚖",
        "rapido" to "Rapido 🛵",
        "makemytrip" to "MakeMyTrip ✈️",
        "goibibo" to "Goibibo ✈️",
        "indigo" to "IndiGo ✈️",
        "air india" to "Air India ✈️",

        // Shopping & Utilities
        "amazon" to "Amazon 📦",
        "flipkart" to "Flipkart 📦",
        "myntra" to "Myntra 👗",
        "ajio" to "Ajio 🛍️",
        "nykaa" to "Nykaa 💄",
        "cred" to "CRED 💳",
        "airtel" to "Airtel 📱",
        "jio" to "Jio 📱"
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

        // 1. Indian Railway / IRCTC truncation fixes ("Indian Rail W", "INDIAN RAILWAYS", "IRCTC", etc.)
        if (lowerCleaned.contains("indian rail") || lowerCleaned.contains("railway") || lowerCleaned.contains("irctc")) {
            return "Indian Railway 🚂"
        }

        // 2. Check brand mappings
        for ((key, brand) in VPA_BRAND_MAP) {
            if (lowerCleaned.contains(key)) return brand
        }

        // Clean trailing city or terminal codes (e.g. "SWIGGY BANGALORE IN" -> "Swiggy")
        cleaned = cleaned.replace(Regex("\\s+(?:IN|IND|BANGALORE|MUMBAI|DELHI|HYDERABAD|PUNE|CHENNAI)$", RegexOption.IGNORE_CASE), "")

        return if (cleaned.isBlank()) "Merchant" else cleaned
    }
}
