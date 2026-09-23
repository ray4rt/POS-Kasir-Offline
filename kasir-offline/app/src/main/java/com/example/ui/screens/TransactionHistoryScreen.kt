package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopSettings
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@Composable
fun TransactionHistoryScreen(
    transactions: List<TransactionEntity>,
    settings: ShopSettings?,
    currentUser: UserAccount,
    onCancelTransaction: (TransactionEntity, reason: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Semua") }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToCancel by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedMethod) {
        transactions.filter { t ->
            (selectedMethod == "Semua" || t.paymentMethod.equals(selectedMethod, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             t.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
             (t.customerName ?: "").contains(searchQuery, ignoreCase = true) ||
             t.cashierName.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat Transaksi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Lihat detail struk, cetak ulang, atau batalkan transaksi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari no invoice, kasir, atau pelanggan...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Payment Method Filter Chips
        val methods = listOf("Semua", "Cash", "QRIS", "Transfer", "Debit", "Kredit", "E-wallet")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            items(methods) { method ->
                FilterChip(
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    label = { Text(method) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Text(
                    text = "Tidak ada transaksi yang ditemukan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(filteredTransactions) { trans ->
                    val isCancelled = trans.status == "CANCELLED"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTransactionForDetail = trans }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp).fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = trans.invoiceNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isCancelled) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = trans.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${CurrencyFormatter.formatDate(trans.timestamp)} • Kasir: ${trans.cashierName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!trans.customerName.isNullOrBlank()) {
                                    Text(
                                        text = "Pelanggan: ${trans.customerName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.format(trans.totalAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCancelled) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldPrimary
                                )
                                Text(
                                    text = trans.paymentMethod,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row {
                                    IconButton(
                                        onClick = { selectedTransactionForReceipt = trans },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "Cetak Struk", modifier = Modifier.size(18.dp))
                                    }

                                    if (!isCancelled && currentUser.role == "ADMIN") {
                                        IconButton(
                                            onClick = { transactionToCancel = trans },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Batalkan", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction Detail Modal
        selectedTransactionForDetail?.let { trans ->
            AlertDialog(
                onDismissRequest = { selectedTransactionForDetail = null },
                title = {
                    Text("Detail Transaksi: ${trans.invoiceNumber}", fontWeight = FontWeight.Bold)
                },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text("Waktu: ${CurrencyFormatter.formatDate(trans.timestamp)}")
                            Text("Kasir: ${trans.cashierName}")
                            if (!trans.customerName.isNullOrBlank()) Text("Pelanggan: ${trans.customerName}")
                            Text("Metode Pembayaran: ${trans.paymentMethod}")
                            Text("Status: ${trans.status}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Daftar Item:", fontWeight = FontWeight.Bold)
                        }

                        items(trans.getItems()) { item ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${UnitConverter.formatQty(item.quantity)} ${item.selectedUnit} @ ${CurrencyFormatter.format(item.unitPrice)}/${item.baseUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(CurrencyFormatter.format(item.subtotal), fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Subtotal:")
                                Text(CurrencyFormatter.format(trans.subtotal))
                            }
                            if (trans.discountAmount > 0) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Diskon:")
                                    Text("-${CurrencyFormatter.format(trans.discountAmount)}")
                                }
                            }
                            if (trans.taxAmount > 0) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Pajak:")
                                    Text(CurrencyFormatter.format(trans.taxAmount))
                                }
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Akhir:", fontWeight = FontWeight.Bold)
                                Text(CurrencyFormatter.format(trans.totalAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Bayar:")
                                Text(CurrencyFormatter.format(trans.paidAmount))
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Kembalian:")
                                Text(CurrencyFormatter.format(trans.changeAmount))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedTransactionForReceipt = trans
                            selectedTransactionForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Buka Struk")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTransactionForDetail = null }) {
                        Text("Tutup")
                    }
                }
            )
        }

        // Receipt dialog
        selectedTransactionForReceipt?.let { trans ->
            ThermalReceiptDialog(
                transaction = trans,
                settings = settings ?: ShopSettings(),
                onDismiss = { selectedTransactionForReceipt = null }
            )
        }

        // Cancel Transaction Modal (Admin Only)
        transactionToCancel?.let { trans ->
            var cancelReason by remember { mutableStateOf("Salah input barang") }

            AlertDialog(
                onDismissRequest = { transactionToCancel = null },
                title = { Text("Batalkan Transaksi?", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Membatalkan transaksi ${trans.invoiceNumber} senilai ${CurrencyFormatter.format(trans.totalAmount)} akan mengembalikan stok semua produk yang dibeli.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cancelReason,
                            onValueChange = { cancelReason = it },
                            label = { Text("Alasan Pembatalan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCancelTransaction(trans, cancelReason)
                            transactionToCancel = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Ya, Batalkan Transaksi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToCancel = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
