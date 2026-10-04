package com.vyayah.app.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.AccountDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CombinedCardMetrics(
    val totalLimitMinor: Long = 0L,
    val totalOutstandingMinor: Long = 0L,
    val totalAvailableLimitMinor: Long = 0L,
    val utilizationPercentage: Float = 0f,
    val cardCount: Int = 0
)

class CardsViewModel(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = accountDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val combinedCardMetrics: StateFlow<CombinedCardMetrics> = accounts.map { list ->
        val creditCards = list.filter { it.type == AccountType.CREDIT }
        var totalLimit = 0L
        var totalOutstanding = 0L

        for (card in creditCards) {
            totalLimit += (card.creditLimit ?: 0L)
            totalOutstanding += (card.outstanding ?: 0L)
        }

        val available = (totalLimit - totalOutstanding).coerceAtLeast(0L)
        val utilPct = if (totalLimit > 0) (totalOutstanding.toFloat() / totalLimit) * 100f else 0f

        CombinedCardMetrics(
            totalLimitMinor = totalLimit,
            totalOutstandingMinor = totalOutstanding,
            totalAvailableLimitMinor = available,
            utilizationPercentage = utilPct,
            cardCount = creditCards.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CombinedCardMetrics())

    fun addOrUpdateAccount(account: Account) {
        viewModelScope.launch {
            accountDao.insert(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountDao.delete(account)
        }
    }

    fun updateBalance(accountId: Long, newBalancePaise: Long) {
        viewModelScope.launch {
            accountDao.updateBalance(accountId, newBalancePaise, System.currentTimeMillis())
        }
    }
}
