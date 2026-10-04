package com.vyayah.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vyayah.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        Transaction::class,
        Account::class,
        Category::class,
        MerchantRule::class,
        Budget::class,
        Goal::class,
        SenderRule::class,
        SyncState::class,
        Trip::class,
        TripTransaction::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VyayahDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun ruleDao(): RuleDao
    abstract fun syncDao(): SyncDao
    abstract fun tripDao(): TripDao


    companion object {
        private const val DB_NAME = "vyayah_encrypted.db"

        @Volatile
        private var INSTANCE: VyayahDatabase? = null

        fun getInstance(context: Context, useEncryption: Boolean = true): VyayahDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, useEncryption).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context, useEncryption: Boolean): VyayahDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                VyayahDatabase::class.java,
                DB_NAME
            )

            if (useEncryption) {
                val passphrase = DatabaseKeyManager.getDatabasePassphrase(context.applicationContext)
                val factory = SupportOpenHelperFactory(passphrase)
                builder.openHelperFactory(factory)
            }

            builder.fallbackToDestructiveMigration()


            builder.addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = getInstance(context, useEncryption)
                        seedDefaults(database)
                    }
                }
            })

            return builder.build()
        }

        suspend fun seedDefaults(database: VyayahDatabase) {
            // Seed Categories
            database.categoryDao().insertAll(Category.DEFAULT_CATEGORIES)

            // Seed Bank Sender Allowlist rules
            val defaultSenders = listOf(
                SenderRule(senderPattern = ".*HDFCBK.*", allowed = true, bankName = "HDFC Bank"),
                SenderRule(senderPattern = ".*SBINB.*", allowed = true, bankName = "State Bank of India"),
                SenderRule(senderPattern = ".*SBIUPI.*", allowed = true, bankName = "SBI UPI"),
                SenderRule(senderPattern = ".*ICICIB.*", allowed = true, bankName = "ICICI Bank"),
                SenderRule(senderPattern = ".*AXISBK.*", allowed = true, bankName = "Axis Bank"),
                SenderRule(senderPattern = ".*KOTAKB.*", allowed = true, bankName = "Kotak Mahindra Bank"),
                SenderRule(senderPattern = ".*PNBSMS.*", allowed = true, bankName = "Punjab National Bank"),
                SenderRule(senderPattern = ".*IDFCFB.*", allowed = true, bankName = "IDFC FIRST Bank"),
                SenderRule(senderPattern = ".*BOBTXN.*", allowed = true, bankName = "Bank of Baroda"),
                SenderRule(senderPattern = ".*INDUSB.*", allowed = true, bankName = "IndusInd Bank"),
                SenderRule(senderPattern = ".*CANBNK.*", allowed = true, bankName = "Canara Bank"),
                SenderRule(senderPattern = ".*UNIONB.*", allowed = true, bankName = "Union Bank of India")
            )
            database.ruleDao().insertAllSenderRules(defaultSenders)
        }
    }
}
