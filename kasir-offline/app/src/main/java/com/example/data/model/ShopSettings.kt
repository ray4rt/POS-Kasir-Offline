package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "Toko Berkah Bersama",
    val address: String = "Jl. Sudirman No. 123, Jakarta",
    val phone: String = "0851-5087-2460",
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar.",
    val currency: String = "Rp",
    val taxPercentage: Double = 0.0,
    val printerWidth: String = "58mm",    // "58mm" or "80mm"
    val isDarkMode: Boolean = true,
    val autoPrintReceipt: Boolean = false
)
