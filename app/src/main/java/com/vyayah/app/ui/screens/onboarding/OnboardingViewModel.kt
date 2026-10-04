package com.vyayah.app.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.AccountDao
import com.vyayah.app.data.local.GoalDao
import com.vyayah.app.data.local.SyncDao
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import com.vyayah.app.data.model.Goal
import com.vyayah.app.parser.AmountParser
import com.vyayah.app.worker.CatchUpSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingAccountDraft(
    val bank: String,
    val last4: String,
    val balanceRupees: String,
    val isCreditCard: Boolean = false,
    val creditLimitRupees: String = "",
    val statementDay: String = "",
    val dueDay: String = ""
)

data class OnboardingGoalDraft(
    val name: String,
    val emoji: String = "🎯",
    val targetRupees: String
)

class OnboardingViewModel(
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val syncDao: SyncDao
) : ViewModel() {

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _backfillMonths = MutableStateFlow(3)
    val backfillMonths: StateFlow<Int> = _backfillMonths.asStateFlow()

    fun nextStep() {
        if (_step.value < 4) {
            _step.value = _step.value + 1
        }
    }

    fun previousStep() {
        if (_step.value > 0) {
            _step.value = _step.value - 1
        }
    }

    fun setBackfillMonths(months: Int) {
        _backfillMonths.value = months
    }

    fun finishOnboarding(
        context: Context,
        bankAccounts: List<OnboardingAccountDraft>,
        cards: List<OnboardingAccountDraft>,
        goal: OnboardingGoalDraft?,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            // Save Bank Accounts
            for (draft in bankAccounts) {
                val balMinor = AmountParser.parseToMinorUnits(draft.balanceRupees.ifBlank { "0" })
                accountDao.insert(
                    Account(
                        bank = draft.bank.ifBlank { "Bank" },
                        type = AccountType.SAVINGS,
                        last4 = draft.last4.ifBlank { "0000" },
                        nickname = "${draft.bank} ••${draft.last4}",
                        openingBalance = balMinor,
                        currentBalance = balMinor
                    )
                )
            }

            // Save Credit & Debit Cards
            for (card in cards) {
                val balMinor = AmountParser.parseToMinorUnits(card.balanceRupees.ifBlank { "0" })
                val limitMinor = AmountParser.parseToMinorUnits(card.creditLimitRupees.ifBlank { "0" })
                accountDao.insert(
                    Account(
                        bank = card.bank.ifBlank { "Bank" },
                        type = if (card.isCreditCard) AccountType.CREDIT else AccountType.DEBIT,
                        last4 = card.last4.ifBlank { "0000" },
                        nickname = "${card.bank} ${if (card.isCreditCard) "Card" else "Debit"} ••${card.last4}",
                        openingBalance = balMinor,
                        currentBalance = balMinor,
                        creditLimit = if (card.isCreditCard) limitMinor else null,
                        outstanding = if (card.isCreditCard) balMinor else null,
                        statementDay = card.statementDay.toIntOrNull(),
                        dueDay = card.dueDay.toIntOrNull()
                    )
                )
            }

            // Save Goal if provided
            if (goal != null && goal.name.isNotBlank()) {
                val targetMinor = AmountParser.parseToMinorUnits(goal.targetRupees.ifBlank { "0" })
                if (targetMinor > 0) {
                    goalDao.insert(
                        Goal(
                            name = goal.name.trim(),
                            emoji = goal.emoji,
                            targetAmountMinor = targetMinor
                        )
                    )
                }
            }

            // Trigger Backfill scan via CatchUpSyncWorker
            CatchUpSyncWorker.triggerOneTimeSync(context, forceFullScan = true)

            // Mark onboarding as completed in SharedPreferences
            context.getSharedPreferences("vyayah_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_complete", true)
                .apply()

            onFinished()
        }
    }
}
