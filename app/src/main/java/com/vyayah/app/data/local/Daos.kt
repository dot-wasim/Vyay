package com.vyayah.app.data.local

import androidx.room.*
import com.vyayah.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE smsHash = :smsHash LIMIT 1")
    suspend fun getBySmsHash(smsHash: String): Transaction?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'NEEDS_REVIEW' ORDER BY timestamp DESC")
    fun getNeedsReviewTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE upiRef = :upiRef LIMIT 1")
    suspend fun getByUpiRef(upiRef: String): Transaction?

    @Query("""
        SELECT * FROM transactions 
        WHERE direction = 'DEBIT' 
          AND amountMinor >= :refundAmountMinor 
          AND timestamp BETWEEN (:refundTime - :windowMillis) AND :refundTime
        ORDER BY timestamp DESC
    """)
    suspend fun findPotentialDebitMatchesForRefund(
        refundAmountMinor: Long,
        refundTime: Long,
        windowMillis: Long = 60L * 24 * 60 * 60 * 1000 // 60 days
    ): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY bank ASC, last4 ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): Account?

    @Query("SELECT * FROM accounts WHERE last4 = :last4 LIMIT 1")
    suspend fun getByLast4(last4: String): Account?

    @Query("SELECT * FROM accounts WHERE bank = :bank AND last4 = :last4 LIMIT 1")
    suspend fun getByBankAndLast4(bank: String, last4: String): Account?

    @Query("UPDATE accounts SET currentBalance = :newBalance, lastReconciledAt = :timestamp WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Long, timestamp: Long)

    @Query("UPDATE accounts SET outstanding = :newOutstanding WHERE id = :id")
    suspend fun updateOutstanding(id: Long, newOutstanding: Long)
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getByCategory(categoryId: Long?): Budget?
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals ORDER BY status ASC, targetDate ASC")
    fun getAllGoals(): Flow<List<Goal>>
}

@Dao
interface RuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantRule(rule: MerchantRule): Long

    @Query("SELECT * FROM merchant_rules ORDER BY priority DESC")
    fun getAllMerchantRules(): Flow<List<MerchantRule>>

    @Query("SELECT * FROM merchant_rules ORDER BY priority DESC")
    suspend fun getMerchantRulesSnapshot(): List<MerchantRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSenderRule(rule: SenderRule): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllSenderRules(rules: List<SenderRule>)

    @Query("SELECT * FROM sender_rules")
    fun getAllSenderRules(): Flow<List<SenderRule>>

    @Query("SELECT * FROM sender_rules")
    suspend fun getSenderRulesSnapshot(): List<SenderRule>
}

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(syncState: SyncState)

    @Query("SELECT * FROM sync_state WHERE `key` = :key LIMIT 1")
    suspend fun getSyncState(key: String = "default_sync"): SyncState?
}

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY isActive DESC, id DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: Long): Trip?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun tagTransaction(tripTransaction: TripTransaction)

    @Query("DELETE FROM trip_transactions WHERE tripId = :tripId AND transactionId = :transactionId")
    suspend fun untagTransaction(tripId: Long, transactionId: Long)

    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN trip_transactions tt ON t.id = tt.transactionId
        WHERE tt.tripId = :tripId
        ORDER BY t.timestamp DESC
    """)
    fun getTransactionsForTrip(tripId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM trip_transactions WHERE tripId = :tripId")
    suspend fun getTripTransactions(tripId: Long): List<TripTransaction>

    @Query("UPDATE trips SET spentMinor = :spentMinor WHERE id = :tripId")
    suspend fun updateTripSpent(tripId: Long, spentMinor: Long)
}

