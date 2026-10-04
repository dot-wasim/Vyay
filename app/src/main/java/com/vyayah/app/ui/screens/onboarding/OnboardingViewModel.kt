package com.vyayah.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.AccountDao
import com.vyayah.app.data.local.GoalDao
import com.vyayah.app.data.local.SyncDao
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import com.vyayah.app.data.model.Goal
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingAccountDraft(
    val bank: String,
    val last4: String,
    val balanceRupees: String,
    val isCreditCard: Boolean = false
)

class OnboardingViewModel(
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val syncDao: SyncDao
) : ViewModel() {

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step.asStateFlow()

    fun nextStep() {
        _step.value = _step.value + 1
    }

    fun previousStep() {
        if (_step.value > 0) _step.value = _step.value - 1
    }

    fun saveAccountsAndFinish(accounts: List<OnboardingAccountDraft>, onFinished: () -> Unit) {
        viewModelScope.launch {
            for (draft in accounts) {
                val balMinor = AmountParser.parseToMinorUnits(draft.balanceRupees.ifBlank { "0" })
                accountDao.insert(
                    Account(
                        bank = draft.bank.ifBlank { "Bank" },
                        type = if (draft.isCreditCard) AccountType.CREDIT else AccountType.SAVINGS,
                        last4 = draft.last4.ifBlank { "0000" },
                        nickname = "${draft.bank} ${draft.last4}",
                        openingBalance = balMinor,
                        currentBalance = balMinor,
                        outstanding = if (draft.isCreditCard) balMinor else null
                    )
                )
            }
            onFinished()
        }
    }
}
