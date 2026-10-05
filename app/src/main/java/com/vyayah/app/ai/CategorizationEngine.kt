package com.vyayah.app.ai

import com.vyayah.app.data.local.CategoryDao
import com.vyayah.app.data.local.RuleDao
import com.vyayah.app.data.model.MerchantRule

class CategorizationEngine(
    private val categoryDao: CategoryDao,
    private val ruleDao: RuleDao
) {

    private val BUILTIN_MERCHANT_CATEGORIES = mapOf(
        // Food & Dining
        "swiggy" to "Food & Dining",
        "zomato" to "Food & Dining",
        "mcdonalds" to "Food & Dining",
        "starbucks" to "Food & Dining",
        "dominos" to "Food & Dining",
        "eats" to "Food & Dining",

        // Groceries
        "blinkit" to "Groceries",
        "zepto" to "Groceries",
        "bigbasket" to "Groceries",
        "instamart" to "Groceries",
        "dmart" to "Groceries",
        "nature basket" to "Groceries",

        // Transport
        "uber" to "Transport",
        "ola" to "Transport",
        "rapido" to "Transport",
        "metro" to "Transport",
        "indian railway" to "Transport",
        "railway" to "Transport",
        "train" to "Transport",
        "irctc" to "Transport",
        "fuel" to "Transport",
        "petrol" to "Transport",
        "hpcl" to "Transport",
        "bpcl" to "Transport",
        "ioc" to "Transport",

        // Subscriptions
        "netflix" to "Subscriptions",
        "spotify" to "Subscriptions",
        "amazon prime" to "Subscriptions",
        "hotstar" to "Subscriptions",
        "apple.com/bill" to "Subscriptions",
        "google storage" to "Subscriptions",
        "youtube" to "Subscriptions",

        // Shopping
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "myntra" to "Shopping",
        "ajio" to "Shopping",
        "nykaa" to "Shopping",
        "tata cliq" to "Shopping",

        // Bills & Utilities
        "bescom" to "Bills & Utilities",
        "tneb" to "Bills & Utilities",
        "mahavitaran" to "Bills & Utilities",
        "airtel" to "Bills & Utilities",
        "jio" to "Bills & Utilities",
        "vi prepaid" to "Bills & Utilities",
        "tatasky" to "Bills & Utilities",
        "cred" to "Bills & Utilities",

        // Health
        "apollo" to "Health",
        "pharmeasy" to "Health",
        "1mg" to "Health",
        "medplus" to "Health",
        "netmeds" to "Health",
        "hospital" to "Health",

        // Entertainment
        "bookmyshow" to "Entertainment",
        "pvr" to "Entertainment",
        "inox" to "Entertainment",
        "steam" to "Entertainment",

        // Travel
        "makemytrip" to "Travel",
        "goibibo" to "Travel",
        "indigo" to "Travel",
        "air india" to "Travel",
        "cleartrip" to "Travel",
        "yatra" to "Travel"
    )

    suspend fun categorize(merchantNorm: String?, rawBody: String?): Long? {
        val rules = ruleDao.getMerchantRulesSnapshot()

        // 1. Check user-defined rules first
        if (!merchantNorm.isNullOrBlank()) {
            val userRule = rules.firstOrNull { rule ->
                if (rule.isRegex) {
                    Regex(rule.pattern, RegexOption.IGNORE_CASE).containsMatchIn(merchantNorm)
                } else {
                    merchantNorm.contains(rule.pattern, ignoreCase = true)
                }
            }
            if (userRule != null) return userRule.categoryId
        }

        // 2. Check built-in merchant map
        if (!merchantNorm.isNullOrBlank()) {
            val lowerNorm = merchantNorm.lowercase()
            for ((key, catName) in BUILTIN_MERCHANT_CATEGORIES) {
                if (lowerNorm.contains(key)) {
                    val cat = categoryDao.getByName(catName)
                    if (cat != null) return cat.id
                }
            }
        }

        // 3. Check SMS body keywords
        if (!rawBody.isNullOrBlank()) {
            val lowerBody = rawBody.lowercase()
            for ((key, catName) in BUILTIN_MERCHANT_CATEGORIES) {
                if (lowerBody.contains(key)) {
                    val cat = categoryDao.getByName(catName)
                    if (cat != null) return cat.id
                }
            }
        }

        // Default to "Other"
        val otherCategory = categoryDao.getByName("Other")
        return otherCategory?.id
    }

    suspend fun teachMerchantRule(merchantPattern: String, categoryId: Long) {
        ruleDao.insertMerchantRule(
            MerchantRule(
                pattern = merchantPattern.trim(),
                categoryId = categoryId,
                priority = 10,
                isRegex = false
            )
        )
    }
}
