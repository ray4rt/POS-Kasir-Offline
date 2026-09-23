package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val oldCostPrice: Double,
    val newCostPrice: Double,
    val oldSellingPrice: Double,
    val newSellingPrice: Double,
    val unit: String,
    val changedBy: String = "Admin",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
