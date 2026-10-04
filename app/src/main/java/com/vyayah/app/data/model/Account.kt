package com.vyayah.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class AccountType {
    SAVINGS,
    CURRENT,
    CREDIT,
    DEBIT
}

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["bank", "last4"], unique = true)
    ]
)
@Serializable
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bank: String,
    val type: AccountType,
    val last4: String,
    val nickname: String,
    val openingBalance: Long = 0L, // In paise
    val openingAt: Long = System.currentTimeMillis(),
    val currentBalance: Long = 0L, // In paise
    val lastSmsBalance: Long? = null,
    val lastReconciledAt: Long? = null,
    val creditLimit: Long? = null,
    val outstanding: Long? = null,
    val statementDay: Int? = null,
    val dueDay: Int? = null,
    val includeInTotal: Boolean = true
)
