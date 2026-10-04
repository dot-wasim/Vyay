package com.vyayah.app.ui.screens.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.CategoryDao
import com.vyayah.app.data.local.RuleDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.*
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LedgerItem(
    val transaction: Transaction,
    val categoryName: String,
    val categoryColor: Long
)

class LedgerViewModel(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val ruleDao: RuleDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val needsReviewCount: StateFlow<Int> = transactionDao.getNeedsReviewTransactions()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val transactions: StateFlow<List<LedgerItem>> = combine(
        transactionDao.getAllTransactions(),
        categoryDao.getAllCategories(),
        _searchQuery,
        _selectedCategoryId
    ) { txns, cats, query, selectedCat ->
        val catMap = cats.associateBy { it.id }
        txns.filter { tx ->
            val matchesQuery = query.isBlank() ||
                (tx.merchantNorm?.contains(query, ignoreCase = true) == true) ||
                (tx.upiVpa?.contains(query, ignoreCase = true) == true) ||
                (tx.sender.contains(query, ignoreCase = true))

            val matchesCat = selectedCat == null || tx.categoryId == selectedCat

            matchesQuery && matchesCat
        }.map { tx ->
            val cat = catMap[tx.categoryId]
            LedgerItem(
                transaction = tx,
                categoryName = cat?.name ?: "Other",
                categoryColor = cat?.color ?: 0xFF607D8B
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    /**
     * Adds a manually entered transaction (missed SMS or cash purchase).
     */
    fun addMissedTransaction(
        amountRupees: String,
        merchantName: String,
        direction: TransactionDirection,
        instrument: PaymentInstrument,
        categoryId: Long?,
        notes: String?
    ) {
        viewModelScope.launch {
            val amountMinor = AmountParser.parseToMinorUnits(amountRupees.trim())
            if (amountMinor <= 0L) return@launch

            val now = System.currentTimeMillis()
            val newTxn = Transaction(
                smsHash = "manual_${now}_${(1000..9999).random()}",
                sender = "Manual Entry",
                rawBody = notes ?: "Manually entered transaction",
                timestamp = now,
                amountMinor = amountMinor,
                currency = "INR",
                direction = direction,
                type = if (direction == TransactionDirection.DEBIT) TransactionType.PURCHASE else TransactionType.INCOME,
                instrument = instrument,
                merchantRaw = merchantName.trim(),
                merchantNorm = merchantName.trim().ifBlank { "Uncategorized" },
                categoryId = categoryId,
                status = TransactionStatus.CONFIRMED,
                source = ParseSource.MANUAL,
                confidence = 1.0f,
                notes = notes
            )
            transactionDao.insert(newTxn)
        }
    }

    /**
     * Renames any transaction and optionally updates future merchant naming rules.
     */
    fun renameTransaction(transaction: Transaction, newName: String, teachRule: Boolean = false) {
        viewModelScope.launch {
            val cleanName = newName.trim()
            if (cleanName.isBlank()) return@launch

            transactionDao.update(
                transaction.copy(
                    merchantNorm = cleanName,
                    status = TransactionStatus.CONFIRMED
                )
            )

            if (teachRule && !transaction.merchantRaw.isNullOrBlank() && transaction.categoryId != null) {
                ruleDao.insertMerchantRule(
                    MerchantRule(
                        pattern = transaction.merchantRaw,
                        categoryId = transaction.categoryId,
                        priority = 15,
                        isRegex = false
                    )
                )
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }

    fun updateCategory(transaction: Transaction, newCategoryId: Long, alwaysCategorize: Boolean) {
        viewModelScope.launch {
            transactionDao.update(
                transaction.copy(
                    categoryId = newCategoryId,
                    status = TransactionStatus.CONFIRMED
                )
            )
            if (alwaysCategorize && !transaction.merchantNorm.isNullOrBlank()) {
                ruleDao.insertMerchantRule(
                    MerchantRule(
                        pattern = transaction.merchantNorm,
                        categoryId = newCategoryId,
                        priority = 10,
                        isRegex = false
                    )
                )
            }
        }
    }
}
