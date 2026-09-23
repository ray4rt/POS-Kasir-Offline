package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.TransactionEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun DashboardScreen(
    products: List<Product>,
    transactions: List<TransactionEntity>,
    onViewAllTransactions: () -> Unit,
    onViewProductDetails: (Product) -> Unit
) {
    var periodFilter by remember { mutableStateOf("Hari Ini") } // "Hari Ini", "7 Hari", "Bulan Ini"

    // Time filter calculation
    val now = System.currentTimeMillis()
    val filteredTransactions = remember(transactions, periodFilter) {
        val cal = Calendar.getInstance()
        when (periodFilter) {
            "Hari Ini" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfDay && it.status == "COMPLETED" }
            }
            "7 Hari" -> {
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                transactions.filter { it.timestamp >= sevenDaysAgo && it.status == "COMPLETED" }
            }
            else -> { // "Bulan Ini"
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfMonth = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfMonth && it.status == "COMPLETED" }
            }
        }
    }

    val totalSales = filteredTransactions.sumOf { it.totalAmount }
    val totalTransactionsCount = filteredTransactions.size
    val totalProfit = filteredTransactions.sumOf { it.profit }
    val totalActiveProducts = products.count { it.isActive }

    // Best selling products calculation from items
    val productSalesMap = remember(transactions) {
        val map = mutableMapOf<String, Double>()
        transactions.filter { it.status == "COMPLETED" }.forEach { trans ->
            trans.getItems().forEach { item ->
                map[item.productName] = (map[item.productName] ?: 0.0) + item.quantity
            }
        }
        map.toList().sortedByDescending { it.second }.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Welcome Header & Period Filter
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Dashboard Penjualan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ringkasan data usaha retail Anda secara real-time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hari Ini", "7 Hari", "Bulan Ini").forEach { period ->
                        FilterChip(
                            selected = periodFilter == period,
                            onClick = { periodFilter = period },
                            label = { Text(period) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Summary Metric Cards Grid
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Total Penjualan",
                        value = CurrencyFormatter.format(totalSales),
                        icon = Icons.Default.AttachMoney,
                        iconColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Jumlah Transaksi",
                        value = "$totalTransactionsCount",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Estimasi Keuntungan",
                        value = CurrencyFormatter.format(totalProfit),
                        icon = Icons.Default.TrendingUp,
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Produk Aktif",
                        value = "$totalActiveProducts",
                        icon = Icons.Default.Inventory2,
                        iconColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sales Visual Summary Bar Chart
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aktivitas Penjualan ($periodFilter)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredTransactions.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text(
                                text = "Belum ada transaksi pada periode ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Simple Visual Distribution Bars
                        val paymentDistribution = filteredTransactions.groupBy { it.paymentMethod }
                        paymentDistribution.forEach { (method, list) ->
                            val methodTotal = list.sumOf { it.totalAmount }
                            val ratio = if (totalSales > 0) (methodTotal / totalSales).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = method, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "${CurrencyFormatter.format(methodTotal)} (${(ratio * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio)
                                            .height(8.dp)
                                            .background(EmeraldPrimary, RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Selling Products
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Produk Terlaris",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (productSalesMap.isEmpty()) {
                        Text(
                            text = "Belum ada data penjualan produk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        productSalesMap.forEachIndexed { index, (name, qty) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (index == 0) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (index == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(name, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = "Terjual: ${String.format(java.util.Locale.US, "%.1f", qty).trimEnd('0').trimEnd('.')}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (index < productSalesMap.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions list
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldPrimary,
                    modifier = Modifier.clickable(onClick = onViewAllTransactions)
                )
            }
        }

        val recent = transactions.take(5)
        if (recent.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Text(
                            text = "Belum ada transaksi tersimpan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recent) { trans ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp).fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = trans.invoiceNumber,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${CurrencyFormatter.formatDate(trans.timestamp)} • ${trans.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.format(trans.totalAmount),
                                fontWeight = FontWeight.Bold,
                                color = if (trans.status == "COMPLETED") EmeraldPrimary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = trans.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (trans.status == "COMPLETED") Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
