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

data class CardsScreenSummary(
    val totalCreditLimitMinor: Long = 0L,
    val totalCreditRemainingMinor: Long = 0L,
    val totalCreditDuesMinor: Long = 0L,
    val creditUtilizationPct: Float = 0f,
    val creditCardCount: Int = 0,
    val totalMoneyInBankMinor: Long = 0L,
    val bankAccountCount: Int = 0
)

class CardsViewModel(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = accountDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<Account>> = accounts.map { list ->
        list.filter { it.type != AccountType.CREDIT }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditCards: StateFlow<List<Account>> = accounts.map { list ->
        list.filter { it.type == AccountType.CREDIT }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<CardsScreenSummary> = accounts.map { list ->
        val cards = list.filter { it.type == AccountType.CREDIT }
        val banks = list.filter { it.type != AccountType.CREDIT }

        var totalLimit = 0L
        var totalDues = 0L
        for (c in cards) {
            totalLimit += (c.creditLimit ?: 0L)
            totalDues += (c.outstanding ?: 0L)
        }
        val remainingLimit = (totalLimit - totalDues).coerceAtLeast(0L)
        val utilPct = if (totalLimit > 0) (totalDues.toFloat() / totalLimit) * 100f else 0f

        var totalBankMoney = 0L
        for (b in banks) {
            if (b.includeInTotal) {
                totalBankMoney += b.currentBalance
            }
        }

        CardsScreenSummary(
            totalCreditLimitMinor = totalLimit,
            totalCreditRemainingMinor = remainingLimit,
            totalCreditDuesMinor = totalDues,
            creditUtilizationPct = utilPct,
            creditCardCount = cards.size,
            totalMoneyInBankMinor = totalBankMoney,
            bankAccountCount = banks.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CardsScreenSummary())

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
