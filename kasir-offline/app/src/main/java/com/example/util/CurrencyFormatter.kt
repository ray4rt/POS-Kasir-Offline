package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val rupiahSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        currencySymbol = "Rp "
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val rupiahFormat = DecimalFormat("Rp #,##0", rupiahSymbols)
    private val numberOnlyFormat = DecimalFormat("#,##0", rupiahSymbols)

    fun format(amount: Double): String {
        return rupiahFormat.format(amount)
    }

    fun formatNumber(amount: Double): String {
        return numberOnlyFormat.format(amount)
    }

    fun parseAmount(input: String): Double {
        val clean = input.replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun generateInvoiceNumber(): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomDigits = (1000..9999).random()
        return "INV-$datePart-$randomDigits"
    }
}
