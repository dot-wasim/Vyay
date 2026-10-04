package com.vyayah.app.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.data.local.TransactionDao
import com.vyayah.app.data.local.TripDao
import com.vyayah.app.data.model.Trip
import com.vyayah.app.data.model.TripTransaction
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TripTxn(
    val id: Long = 0,
    val title: String,
    val category: String,
    val amountMinor: Long,
    val dateText: String
)

data class TripItem(
    val id: Long,
    val name: String,
    val emoji: String,
    val dateRangeText: String,
    val budgetMinor: Long,
    val spentMinor: Long,
    val isActive: Boolean,
    val transactions: List<TripTxn>
)

class TripsViewModel(
    private val tripDao: TripDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val trips: StateFlow<List<TripItem>> = tripDao.getAllTrips()
        .map { entityList ->
            if (entityList.isEmpty()) {
                seedInitialTrips()
                emptyList()
            } else {
                entityList.map { trip ->
                    val txns = tripDao.getTripTransactions(trip.id).mapNotNull { tt ->
                        transactionDao.getById(tt.transactionId)?.let { t ->
                            TripTxn(
                                id = t.id,
                                title = t.merchantNorm ?: t.merchantRaw ?: "Expense",
                                category = "Travel",
                                amountMinor = t.amountMinor,
                                dateText = dateFormat.format(Date(t.timestamp))
                            )
                        }
                    }
                    val totalSpent = txns.sumOf { it.amountMinor }
                    TripItem(
                        id = trip.id,
                        name = trip.name,
                        emoji = trip.emoji,
                        dateRangeText = trip.dateRangeText.ifBlank { "Upcoming Trip" },
                        budgetMinor = trip.budgetMinor,
                        spentMinor = if (trip.spentMinor > 0) trip.spentMinor else totalSpent,
                        isActive = trip.isActive,
                        transactions = txns
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun seedInitialTrips() {
        viewModelScope.launch {
            val initialTrip1 = Trip(
                name = "Goa Vacation",
                emoji = "🏖️",
                budgetMinor = 4000000L, // ₹40,000
                spentMinor = 2480000L,  // ₹24,800
                dateRangeText = "10 Oct – 15 Oct 2026",
                isActive = true
            )
            val initialTrip2 = Trip(
                name = "Manali Road Trip",
                emoji = "🏔️",
                budgetMinor = 3500000L, // ₹35,000
                spentMinor = 3120000L,  // ₹31,200
                dateRangeText = "15 Aug – 20 Aug 2026",
                isActive = false
            )
            tripDao.insertTrip(initialTrip1)
            tripDao.insertTrip(initialTrip2)
        }
    }

    fun createTrip(name: String, emoji: String, dateRange: String, budgetRupees: String) {
        viewModelScope.launch {
            val budgetMinor = AmountParser.parseToMinorUnits(budgetRupees.trim())
            val trip = Trip(
                name = name.trim(),
                emoji = emoji.ifBlank { "✈️" },
                budgetMinor = budgetMinor,
                spentMinor = 0L,
                dateRangeText = dateRange.ifBlank { "Upcoming Trip" },
                isActive = true
            )
            tripDao.insertTrip(trip)
        }
    }

    fun tagTransactionToTrip(tripId: Long, transactionId: Long) {
        viewModelScope.launch {
            tripDao.tagTransaction(TripTransaction(tripId = tripId, transactionId = transactionId))
        }
    }

    fun untagTransaction(tripId: Long, transactionId: Long) {
        viewModelScope.launch {
            tripDao.untagTransaction(tripId, transactionId)
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            tripDao.getTripById(tripId)?.let { tripDao.deleteTrip(it) }
        }
    }
}
