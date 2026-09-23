package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String,               // "IN", "OUT", "ADJUSTMENT", "SALE", "CANCELLED_RESTORE"
    val quantity: Double,           // quantity moved in base unit
    val previousStock: Double,
    val newStock: Double,
    val unit: String,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val referenceInvoice: String? = null
)
