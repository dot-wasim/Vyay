package com.vyayah.app.ui.screens.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.GoalDao
import com.vyayah.app.data.model.Goal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SaveViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = goalDao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(name: String, targetRupees: String, emoji: String) {
        viewModelScope.launch {
            val amountMinor = com.vyayah.app.parser.AmountParser.parseToMinorUnits(targetRupees)
            goalDao.insert(
                Goal(
                    name = name,
                    emoji = emoji.ifBlank { "🎯" },
                    targetAmountMinor = amountMinor,
                    savedAmountMinor = 0L
                )
            )
        }
    }

    fun contributeToGoal(goal: Goal, addRupees: String) {
        viewModelScope.launch {
            val additionalMinor = com.vyayah.app.parser.AmountParser.parseToMinorUnits(addRupees)
            val updated = goal.copy(savedAmountMinor = goal.savedAmountMinor + additionalMinor)
            goalDao.update(updated)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalDao.delete(goal)
        }
    }
}
