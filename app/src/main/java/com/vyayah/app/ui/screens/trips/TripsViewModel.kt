package com.vyayah.app.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyayah.app.parser.AmountParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripTxn(
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

class TripsViewModel : ViewModel() {

    private val _trips = MutableStateFlow<List<TripItem>>(
        listOf(
            TripItem(
                id = 1,
                name = "Goa Vacation",
                emoji = "🏖️",
                dateRangeText = "10 Oct – 15 Oct 2026",
                budgetMinor = 4000000L, // ₹40,000
                spentMinor = 2480000L,  // ₹24,800
                isActive = true,
                transactions = listOf(
                    TripTxn("IndiGo Airlines", "Flights & Travel", 1120000L, "10 Oct, 6:30 AM"),
                    TripTxn("Taj Holiday Village", "Stays & Hotels", 750000L, "10 Oct, 2:00 PM"),
                    TripTxn("Curlies Beach Shack", "Food & Drinks", 320000L, "11 Oct, 8:45 PM"),
                    TripTxn("Goa Taxi Association", "Transport", 290000L, "11 Oct, 11:30 AM")
                )
            ),
            TripItem(
                id = 2,
                name = "Manali Road Trip",
                emoji = "🏔️",
                dateRangeText = "15 Aug – 20 Aug 2026",
                budgetMinor = 3500000L, // ₹35,000
                spentMinor = 3120000L,  // ₹31,200
                isActive = false,
                transactions = listOf(
                    TripTxn("Fuel HPCL", "Transport", 850000L, "15 Aug"),
                    TripTxn("Himalayan Resort", "Stays", 1450000L, "16 Aug"),
                    TripTxn("Mall Road Cafe", "Food & Dining", 420000L, "17 Aug"),
                    TripTxn("Paragliding Solang", "Activities", 400000L, "18 Aug")
                )
            )
        )
    )
    val trips: StateFlow<List<TripItem>> = _trips.asStateFlow()

    fun createTrip(name: String, emoji: String, dateRange: String, budgetRupees: String) {
        viewModelScope.launch {
            val budgetMinor = AmountParser.parseToMinorUnits(budgetRupees.trim())
            val newTrip = TripItem(
                id = System.currentTimeMillis(),
                name = name.trim(),
                emoji = emoji.ifBlank { "✈️" },
                dateRangeText = dateRange.ifBlank { "Upcoming Trip" },
                budgetMinor = budgetMinor,
                spentMinor = 0L,
                isActive = true,
                transactions = emptyList()
            )
            _trips.value = listOf(newTrip) + _trips.value
        }
    }
}
