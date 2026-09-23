package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "Umum",
    val costPrice: Double = 0.0,      // Harga modal per default unit
    val sellingPrice: Double = 0.0,   // Harga jual per default unit
    val stock: Double = 0.0,          // Stok (bisa desimal, misal 75.5 kg)
    val unit: String = "pcs",         // Satuan default (kg, gram, pcs, liter, dll)
    val minStock: Double = 5.0,       // Minimum stok peringatan
    val description: String = "",
    val isActive: Boolean = true,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
