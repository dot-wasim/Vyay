package com.vyayah.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "merchant_rules",
    indices = [Index(value = ["pattern"], unique = true)]
)
@Serializable
data class MerchantRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    val categoryId: Long,
    val priority: Int = 0,
    val isRegex: Boolean = false
)

enum class BudgetPeriod {
    MONTHLY,
    WEEKLY
}

@Entity(tableName = "budgets")
@Serializable
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long?, // null means overall budget
    val amountMinor: Long, // in paise
    val period: BudgetPeriod = BudgetPeriod.MONTHLY
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED
}

@Entity(tableName = "goals")
@Serializable
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String = "🎯",
    val targetAmountMinor: Long,
    val targetDate: Long? = null,
    val savedAmountMinor: Long = 0L,
    val earmarkAccountId: Long? = null,
    val autoContribute: Boolean = false,
    val status: GoalStatus = GoalStatus.ACTIVE
)

@Entity(
    tableName = "sender_rules",
    indices = [Index(value = ["senderPattern"], unique = true)]
)
@Serializable
data class SenderRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderPattern: String,
    val allowed: Boolean = true,
    val bankName: String? = null
)

@Entity(tableName = "sync_state")
@Serializable
data class SyncState(
    @PrimaryKey
    val key: String = "default_sync",
    val lastProcessedSmsId: Long = 0L,
    val lastProcessedTimestamp: Long = 0L,
    val lastBackfillAt: Long = 0L
)
