package com.vyayah.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "categories")
@Serializable
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // Material icon identifier or emoji
    val color: Long, // ARGB color
    val parentId: Long? = null,
    val isSystem: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(name = "Rent 🏠", icon = "home", color = 0xFF795548, isSystem = true),
            Category(name = "Food & Treats 🍕🍔", icon = "restaurant", color = 0xFFFF5722, isSystem = true),
            Category(name = "Groceries 🛒", icon = "shopping_cart", color = 0xFF4CAF50, isSystem = true),
            Category(name = "Subscriptions 📺", icon = "subscriptions", color = 0xFF9C27B0, isSystem = true),
            Category(name = "Transport 🚕", icon = "directions_car", color = 0xFF2196F3, isSystem = true),
            Category(name = "Bills & Utilities ⚡", icon = "receipt_long", color = 0xFFFF9800, isSystem = true),
            Category(name = "Shopping 🛍️", icon = "shopping_bag", color = 0xFFE91E63, isSystem = true),
            Category(name = "Health 💊", icon = "local_hospital", color = 0xFF009688, isSystem = true),
            Category(name = "Travel ✈️", icon = "flight", color = 0xFF00BCD4, isSystem = true),
            Category(name = "Entertainment & Fun 🍿🎬", icon = "movie", color = 0xFF673AB7, isSystem = true),
            Category(name = "Other 📦", icon = "category", color = 0xFF607D8B, isSystem = true)
        )
    }
}
