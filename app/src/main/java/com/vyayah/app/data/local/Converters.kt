package com.vyayah.app.data.local

import androidx.room.TypeConverter
import com.vyayah.app.data.model.*

class Converters {

    @TypeConverter
    fun fromTransactionDirection(value: TransactionDirection?): String? = value?.name

    @TypeConverter
    fun toTransactionDirection(value: String?): TransactionDirection? =
        value?.let { runCatching { TransactionDirection.valueOf(it) }.getOrDefault(TransactionDirection.DEBIT) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrDefault(TransactionType.PURCHASE) }

    @TypeConverter
    fun fromPaymentInstrument(value: PaymentInstrument?): String? = value?.name

    @TypeConverter
    fun toPaymentInstrument(value: String?): PaymentInstrument? =
        value?.let { runCatching { PaymentInstrument.valueOf(it) }.getOrDefault(PaymentInstrument.UNKNOWN) }

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus?): String? = value?.name

    @TypeConverter
    fun toTransactionStatus(value: String?): TransactionStatus? =
        value?.let { runCatching { TransactionStatus.valueOf(it) }.getOrDefault(TransactionStatus.CONFIRMED) }

    @TypeConverter
    fun fromParseSource(value: ParseSource?): String? = value?.name

    @TypeConverter
    fun toParseSource(value: String?): ParseSource? =
        value?.let { runCatching { ParseSource.valueOf(it) }.getOrDefault(ParseSource.REGEX) }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? =
        value?.let { runCatching { AccountType.valueOf(it) }.getOrDefault(AccountType.SAVINGS) }

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod?): String? = value?.name

    @TypeConverter
    fun toBudgetPeriod(value: String?): BudgetPeriod? =
        value?.let { runCatching { BudgetPeriod.valueOf(it) }.getOrDefault(BudgetPeriod.MONTHLY) }

    @TypeConverter
    fun fromGoalStatus(value: GoalStatus?): String? = value?.name

    @TypeConverter
    fun toGoalStatus(value: String?): GoalStatus? =
        value?.let { runCatching { GoalStatus.valueOf(it) }.getOrDefault(GoalStatus.ACTIVE) }
}
