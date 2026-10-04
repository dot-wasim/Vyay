package com.vyayah.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.AccountDao
import com.vyayah.app.data.local.BudgetDao
import com.vyayah.app.data.local.CategoryDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.Category
import com.vyayah.app.data.model.Transaction
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.data.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySpend(
    val category: Category,
    val amountMinor: Long,
    val percentage: Float
)

data class TodayUiState(
    val totalAvailableBalanceMinor: Long = 0L,
    val totalCreditOutstandingMinor: Long = 0L,
    val monthToDateSpendMinor: Long = 0L,
    val monthIncomeMinor: Long = 0L,
    val netSavingsMinor: Long = 0L,
    val savingsPercentage: Float = 0f,
    val paceVersusLastMonthPercentage: Int = 0,
    val daysLeftInMonth: Int = 0,
    val monthName: String = "",
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val hideAmounts: Boolean = false,
    val lastProcessedTimestamp: Long = 0L
)

class TodayViewModel(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : ViewModel() {

    private val _hideAmounts = MutableStateFlow(false)
    val hideAmounts: StateFlow<Boolean> = _hideAmounts.asStateFlow()

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun toggleHideAmounts() {
        _hideAmounts.value = !_hideAmounts.value
    }

    private fun loadDashboardData() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: "Month"
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = maxDays - currentDay

        // Calculate start of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            combine(
                transactionDao.getTransactionsBetween(startOfMonth, now),
                accountDao.getAllAccounts(),
                categoryDao.getAllCategories(),
                _hideAmounts
            ) { txns, accounts, categories, hide ->
                computeDashboardState(txns, accounts, categories, hide, daysLeft, currentMonth)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun computeDashboardState(
        txns: List<Transaction>,
        accounts: List<Account>,
        categories: List<Category>,
        hide: Boolean,
        daysLeft: Int,
        monthName: String
    ): TodayUiState {
        var mtdSpend = 0L
        var mtdIncome = 0L
        val categoryTotals = mutableMapOf<Long, Long>()

        for (tx in txns) {
            when {
                tx.type == TransactionType.BILL_PAYMENT || tx.type == TransactionType.TRANSFER -> {
                    // Excluded from income and spend
                }
                tx.direction == TransactionDirection.DEBIT -> {
                    mtdSpend += tx.amountMinor
                    val catId = tx.categoryId ?: 0L
                    categoryTotals[catId] = (categoryTotals[catId] ?: 0L) + tx.amountMinor
                }
                tx.type == TransactionType.REFUND || tx.type == TransactionType.REVERSAL -> {
                    // Reduces spend
                    mtdSpend -= tx.amountMinor
                    val catId = tx.categoryId ?: 0L
                    categoryTotals[catId] = (categoryTotals[catId] ?: 0L) - tx.amountMinor
                }
                tx.direction == TransactionDirection.CREDIT -> {
                    mtdIncome += tx.amountMinor
                }
            }
        }

        val netSavings = mtdIncome - mtdSpend
        val savingsRate = if (mtdIncome > 0) ((netSavings.toFloat() / mtdIncome) * 100f).coerceAtLeast(0f) else 0f

        val catMap = categories.associateBy { it.id }
        val categorySpends = categoryTotals.mapNotNull { (catId, amount) ->
            val cat = catMap[catId] ?: Category(name = "Other", icon = "category", color = 0xFF607D8B)
            if (amount > 0 && mtdSpend > 0) {
                CategorySpend(
                    category = cat,
                    amountMinor = amount,
                    percentage = (amount.toFloat() / mtdSpend) * 100f
                )
            } else null
        }.sortedByDescending { it.amountMinor }

        var totalAvlBal = 0L
        var totalOutstanding = 0L
        for (acc in accounts) {
            if (acc.includeInTotal) {
                totalAvlBal += acc.currentBalance
            }
            if (acc.outstanding != null) {
                totalOutstanding += acc.outstanding
            }
        }

        val latestTxnTimestamp = txns.maxOfOrNull { it.timestamp } ?: 0L

        return TodayUiState(
            totalAvailableBalanceMinor = totalAvlBal,
            totalCreditOutstandingMinor = totalOutstanding,
            monthToDateSpendMinor = mtdSpend,
            monthIncomeMinor = mtdIncome,
            netSavingsMinor = netSavings,
            savingsPercentage = savingsRate,
            paceVersusLastMonthPercentage = 4, // Comparison baseline
            daysLeftInMonth = daysLeft,
            monthName = monthName,
            categoryBreakdown = categorySpends,
            hideAmounts = hide,
            lastProcessedTimestamp = latestTxnTimestamp
        )
    }
}
