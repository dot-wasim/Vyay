package com.vyayah.app.ui.screens.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.CategoryDao
import com.vyayah.app.data.local.RuleDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Category
import com.vyayah.app.data.model.MerchantRule
import com.vyayah.app.data.model.Transaction
import com.vyayah.app.data.model.TransactionStatus
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
