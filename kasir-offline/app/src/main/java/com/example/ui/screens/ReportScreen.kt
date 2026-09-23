package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopSettings
import com.example.data.model.TransactionEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun ReportScreen(
    transactions: List<TransactionEntity>,
    settings: ShopSettings?
) {
    val context = LocalContext.current
    var period by remember { mutableStateOf("Hari Ini") } // "Hari Ini", "7 Hari", "Bulan Ini", "Semua"

    val now = System.currentTimeMillis()
    val filtered = remember(transactions, period) {
        val cal = Calendar.getInstance()
        when (period) {
            "Hari Ini" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                transactions.filter { it.timestamp >= cal.timeInMillis && it.status == "COMPLETED" }
            }
            "7 Hari" -> {
                val past = now - (7L * 24 * 60 * 60 * 1000)
                transactions.filter { it.timestamp >= past && it.status == "COMPLETED" }
            }
            "Bulan Ini" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                transactions.filter { it.timestamp >= cal.timeInMillis && it.status == "COMPLETED" }
            }
            else -> transactions.filter { it.status == "COMPLETED" }
        }
    }

    val totalRevenue = filtered.sumOf { it.totalAmount }
    val totalCost = filtered.sumOf { it.totalCost }
    val totalProfit = filtered.sumOf { it.profit }
    val totalDiscount = filtered.sumOf { it.discountAmount }
    val totalTax = filtered.sumOf { it.taxAmount }
    val count = filtered.size

    val reportSummaryText = remember(period, totalRevenue, totalProfit, count) {
        buildString {
            append("=== LAPORAN PENJUALAN (${period.uppercase()}) ===\n")
            append("Toko: ${settings?.shopName ?: "Kasir Offline"}\n")
            append("Waktu Ekspor: ${CurrencyFormatter.formatDate(System.currentTimeMillis())}\n")
            append("--------------------------------------\n")
            append("Total Transaksi : $count\n")
            append("Total Omzet     : ${CurrencyFormatter.format(totalRevenue)}\n")
            append("Total Diskon    : ${CurrencyFormatter.format(totalDiscount)}\n")
            append("Total Pajak     : ${CurrencyFormatter.format(totalTax)}\n")
            append("Estimasi Laba   : ${CurrencyFormatter.format(totalProfit)}\n")
            append("======================================\n")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Laporan Penjualan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Analisis kinerja keuangan & keuntungan usaha",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Hari Ini", "7 Hari", "Bulan Ini", "Semua").forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ringkasan Eksekutif", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Jumlah Transaksi:")
                        Text("$count transaksi", fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Total Omzet (Penjualan):")
                        Text(CurrencyFormatter.format(totalRevenue), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Total Diskon Toko:")
                        Text(CurrencyFormatter.format(totalDiscount))
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Total Pajak Terkumpul:")
                        Text(CurrencyFormatter.format(totalTax))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Estimasi Keuntungan Bersih:", fontWeight = FontWeight.Bold)
                        Text(CurrencyFormatter.format(totalProfit), fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                    }
                }
            }
        }

        // Breakdown by Payment Method
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Penerimaan per Metode Bayar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val group = filtered.groupBy { it.paymentMethod }
                    if (group.isEmpty()) {
                        Text("Belum ada data pada periode ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        group.forEach { (m, list) ->
                            val sum = list.sumOf { it.totalAmount }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("$m (${list.size}x)")
                                Text(CurrencyFormatter.format(sum), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clip.setPrimaryClip(ClipData.newPlainText("Laporan Penjualan", reportSummaryText))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text("Salin Teks")
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Laporan Penjualan $period")
                            putExtra(Intent.EXTRA_TEXT, reportSummaryText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Bagikan Laporan"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text("Bagikan")
                }
            }
        }
    }
}
