package com.example.util

import com.example.data.model.ShopSettings
import com.example.data.model.TransactionEntity

object ReceiptGenerator {

    fun generateTextReceipt(
        transaction: TransactionEntity,
        settings: ShopSettings
    ): String {
        val is58mm = settings.printerWidth == "58mm"
        val width = if (is58mm) 32 else 46
        val line = "=".repeat(width)
        val dash = "-".repeat(width)

        val sb = StringBuilder()

        // Store Header
        sb.append(centerText(settings.shopName.uppercase(), width)).append("\n")
        if (settings.address.isNotBlank()) {
            sb.append(centerText(settings.address, width)).append("\n")
        }
        if (settings.phone.isNotBlank()) {
            sb.append(centerText("Telp: ${settings.phone}", width)).append("\n")
        }
        sb.append(line).append("\n")

        // Metadata
        sb.append(twoColumn("No: ${transaction.invoiceNumber}", "", width)).append("\n")
        sb.append(twoColumn("Tgl: ${CurrencyFormatter.formatDate(transaction.timestamp)}", "Kasir: ${transaction.cashierName}", width)).append("\n")
        if (!transaction.customerName.isNullOrBlank() && transaction.customerName != "-") {
            sb.append(twoColumn("Plg: ${transaction.customerName}", "", width)).append("\n")
        }
        sb.append(dash).append("\n")

        // Items
        val items = transaction.getItems()
        items.forEach { item ->
            // Item Name
            sb.append(item.productName).append("\n")
            // Item Qty x Price and Subtotal
            val qtyStr = "${UnitConverter.formatQty(item.quantity)} ${item.selectedUnit}"
            val priceStr = "@ ${CurrencyFormatter.format(item.unitPrice)}/${item.baseUnit}"
            val detail = "$qtyStr $priceStr"
            val totalStr = CurrencyFormatter.format(item.subtotal)
            sb.append(twoColumn(detail, totalStr, width)).append("\n")
            if (item.discountAmount > 0) {
                sb.append(twoColumn("  (Diskon)", "-${CurrencyFormatter.format(item.discountAmount)}", width)).append("\n")
            }
        }
        sb.append(dash).append("\n")

        // Totals
        sb.append(twoColumn("Subtotal:", CurrencyFormatter.format(transaction.subtotal), width)).append("\n")
        if (transaction.discountAmount > 0) {
            sb.append(twoColumn("Diskon Transaksi:", "-${CurrencyFormatter.format(transaction.discountAmount)}", width)).append("\n")
        }
        if (transaction.taxAmount > 0) {
            sb.append(twoColumn("Pajak:", CurrencyFormatter.format(transaction.taxAmount), width)).append("\n")
        }
        sb.append(line).append("\n")
        sb.append(twoColumn("TOTAL AKHIR:", CurrencyFormatter.format(transaction.totalAmount), width)).append("\n")
        sb.append(twoColumn("Metode Bayar:", transaction.paymentMethod, width)).append("\n")
        sb.append(twoColumn("Bayar:", CurrencyFormatter.format(transaction.paidAmount), width)).append("\n")
        sb.append(twoColumn("Kembalian:", CurrencyFormatter.format(transaction.changeAmount), width)).append("\n")
        sb.append(line).append("\n")

        // Footer
        if (settings.receiptFooter.isNotBlank()) {
            settings.receiptFooter.lines().forEach { footLine ->
                sb.append(centerText(footLine.trim(), width)).append("\n")
            }
        }

        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val leftPadding = (width - text.length) / 2
        val rightPadding = width - text.length - leftPadding
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding)
    }

    private fun twoColumn(left: String, right: String, width: Int): String {
        val available = width - right.length
        if (available <= 0) return (left + " " + right).take(width)
        val leftTrunc = if (left.length > available - 1) left.take(available - 1) else left
        val spaces = " ".repeat((width - leftTrunc.length - right.length).coerceAtLeast(1))
        return leftTrunc + spaces + right
    }
}
