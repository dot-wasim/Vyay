package com.vyayah.app.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.BudgetDao
import com.vyayah.app.data.local.CategoryDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Budget
import com.vyayah.app.data.model.BudgetPeriod
import com.vyayah.app.data.model.Category
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryBudgetItem(
    val categoryId: Long?,
    val categoryName: String,
    val categoryIcon: String,
    val budgetAmountMinor: Long,
    val spentAmountMinor: Long,
    val percentage: Int, // e.g. 85 for 85%
    val isOverBudget: Boolean,
    val isWarning: Boolean // >= 80%
)

data class BudgetUiState(
    val totalBudgetMinor: Long = 0L,
    val totalSpentMinor: Long = 0L,
    val overallPercentage: Int = 0,
    val isTotalWarning: Boolean = false,
    val isTotalOverBudget: Boolean = false,
    val categoryBudgets: List<CategoryBudgetItem> = emptyList(),
    val categories: List<Category> = emptyList()
)

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            // Get current month start and end timestamps
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            combine(
                budgetDao.getAllBudgets(),
                categoryDao.getAllCategories(),
                transactionDao.getTransactionsBetween(monthStart, monthEnd)
            ) { budgets, categories, transactions ->
                val monthDebits = transactions.filter { it.direction == TransactionDirection.DEBIT }

                // Seed default overall budget if completely empty
                if (budgets.isEmpty()) {
                    seedDefaultBudgets(categories)
                }

                val categoryMap = categories.associateBy { it.id }

                val items = budgets.mapNotNull { budget ->
                    if (budget.categoryId == null) return@mapNotNull null // Overall budget handled separately
                    val cat = categoryMap[budget.categoryId] ?: return@mapNotNull null
                    val spent = monthDebits.filter { it.categoryId == cat.id }.sumOf { it.amountMinor }
                    val percent = if (budget.amountMinor > 0) ((spent * 100) / budget.amountMinor).toInt() else 0

                    CategoryBudgetItem(
                        categoryId = cat.id,
                        categoryName = cat.name,
                        categoryIcon = cat.icon,
                        budgetAmountMinor = budget.amountMinor,
                        spentAmountMinor = spent,
                        percentage = percent,
                        isOverBudget = spent >= budget.amountMinor,
                        isWarning = percent in 80..99
                    )
                }

                // Overall budget
                val overallBudget = budgets.find { it.categoryId == null }
                val totalSpent = monthDebits.sumOf { it.amountMinor }
                val totalBudgetMinor = overallBudget?.amountMinor ?: (items.sumOf { it.budgetAmountMinor }.takeIf { it > 0 } ?: 5000000L) // Default ₹50,000
                val overallPercent = if (totalBudgetMinor > 0) ((totalSpent * 100) / totalBudgetMinor).toInt() else 0

                BudgetUiState(
                    totalBudgetMinor = totalBudgetMinor,
                    totalSpentMinor = totalSpent,
                    overallPercentage = overallPercent,
                    isTotalWarning = overallPercent in 80..99,
                    isTotalOverBudget = totalSpent >= totalBudgetMinor,
                    categoryBudgets = items.sortedByDescending { it.percentage },
                    categories = categories
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun seedDefaultBudgets(categories: List<Category>) {
        // Overall monthly budget of ₹50,000
        budgetDao.insert(Budget(categoryId = null, amountMinor = 5000000L, period = BudgetPeriod.MONTHLY))

        // Prepopulate key category budgets
        val defaultAllocations = mapOf(
            "Food & Dining" to 1200000L, // ₹12,000
            "Groceries" to 1000000L,     // ₹10,000
            "Shopping" to 800000L,       // ₹8,000
            "Transport" to 500000L,      // ₹5,000
            "Bills & Utilities" to 600000L // ₹6,000
        )

        categories.forEach { cat ->
            defaultAllocations[cat.name]?.let { limitPaise ->
                budgetDao.insert(Budget(categoryId = cat.id, amountMinor = limitPaise, period = BudgetPeriod.MONTHLY))
            }
        }
    }

    fun setBudget(categoryId: Long?, amountRupees: String) {
        viewModelScope.launch {
            val amountMinor = AmountParser.parseToMinorUnits(amountRupees.trim())
            if (amountMinor <= 0) return@launch

            val existing = budgetDao.getByCategory(categoryId)
            if (existing != null) {
                budgetDao.update(existing.copy(amountMinor = amountMinor))
            } else {
                budgetDao.insert(Budget(categoryId = categoryId, amountMinor = amountMinor, period = BudgetPeriod.MONTHLY))
            }
        }
    }

    fun deleteBudget(categoryId: Long?) {
        viewModelScope.launch {
            budgetDao.getByCategory(categoryId)?.let {
                budgetDao.delete(it)
            }
        }
    }
}
