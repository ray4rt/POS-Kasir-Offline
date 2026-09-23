package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class TransactionItem(
    val productId: Long,
    val productName: String,
    val baseUnit: String,
    val selectedUnit: String,
    val quantity: Double,             // Quantity in selectedUnit (e.g. 250 g or 0.5 kg)
    val baseQuantity: Double,         // Quantity in baseUnit (e.g. 0.25 kg)
    val unitPrice: Double,            // Price per baseUnit
    val discountAmount: Double = 0.0,
    val subtotal: Double,
    val costPrice: Double = 0.0       // Base unit cost price for profit calculation
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("productId", productId)
            put("productName", productName)
            put("baseUnit", baseUnit)
            put("selectedUnit", selectedUnit)
            put("quantity", quantity)
            put("baseQuantity", baseQuantity)
            put("unitPrice", unitPrice)
            put("discountAmount", discountAmount)
            put("subtotal", subtotal)
            put("costPrice", costPrice)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): TransactionItem {
            return TransactionItem(
                productId = json.optLong("productId"),
                productName = json.optString("productName"),
                baseUnit = json.optString("baseUnit"),
                selectedUnit = json.optString("selectedUnit"),
                quantity = json.optDouble("quantity"),
                baseQuantity = json.optDouble("baseQuantity"),
                unitPrice = json.optDouble("unitPrice"),
                discountAmount = json.optDouble("discountAmount", 0.0),
                subtotal = json.optDouble("subtotal"),
                costPrice = json.optDouble("costPrice", 0.0)
            )
        }

        fun listToJson(items: List<TransactionItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<TransactionItem> {
            val list = mutableListOf<TransactionItem>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String = "Kasir",
    val customerName: String? = null,
    val paymentMethod: String = "Cash",    // Cash, QRIS, Transfer, Debit, Kredit, E-wallet, Lainnya
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val notes: String? = null,
    val status: String = "COMPLETED",      // COMPLETED, CANCELLED
    val itemsJson: String = "[]",
    val totalCost: Double = 0.0            // Total cost for profit calculation
) {
    fun getItems(): List<TransactionItem> {
        return TransactionItem.listFromJson(itemsJson)
    }

    val profit: Double
        get() = if (status == "COMPLETED") (totalAmount - totalCost - taxAmount) else 0.0
}
