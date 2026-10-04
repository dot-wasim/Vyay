package com.vyayah.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vyayah.app.R
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BudgetAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "vyayah_budget_alerts"
        const val CHANNEL_NAME = "Budget & Spend Alerts"
    }

    override suspend fun doWork(): Result {
        return try {
            val database = VyayahDatabase.getInstance(context)
            val budgetDao = database.budgetDao()
            val categoryDao = database.categoryDao()
            val transactionDao = database.transactionDao()

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            val budgets = budgetDao.getAllBudgets().first()
            if (budgets.isEmpty()) return Result.success()

            val categories = categoryDao.getAllCategories().first().associateBy { it.id }
            val transactions = transactionDao.getTransactionsBetween(monthStart, monthEnd).first()
            val debits = transactions.filter { it.direction == TransactionDirection.DEBIT }

            ensureNotificationChannel()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            budgets.forEach { budget ->
                val spent = if (budget.categoryId == null) {
                    debits.sumOf { it.amountMinor }
                } else {
                    debits.filter { it.categoryId == budget.categoryId }.sumOf { it.amountMinor }
                }

                if (budget.amountMinor <= 0) return@forEach
                val percentage = (spent * 100) / budget.amountMinor

                val categoryName = if (budget.categoryId == null) "Total Monthly Spend"
                else categories[budget.categoryId]?.name ?: "Budget"

                if (percentage >= 100) {
                    val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle("🚨 Over Budget: $categoryName")
                        .setContentText("You've spent ${AmountParser.formatIndianCurrency(spent)} of your ${AmountParser.formatIndianCurrency(budget.amountMinor)} limit.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(categoryName.hashCode(), notif)
                } else if (percentage >= 80) {
                    val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_more)
                        .setContentTitle("⚠️ Budget Warning: $categoryName")
                        .setContentText("You've reached $percentage% (${AmountParser.formatIndianCurrency(spent)}) of your budget.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(categoryName.hashCode(), notif)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when spending approaches or exceeds your monthly budget limits."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
