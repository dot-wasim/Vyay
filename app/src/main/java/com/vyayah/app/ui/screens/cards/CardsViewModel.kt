package com.vyayah.app.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.AccountDao
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardsViewModel(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = accountDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
