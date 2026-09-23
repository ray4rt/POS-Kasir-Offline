package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StockMovement
import com.example.data.model.UserAccount
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagementScreen(
    products: List<Product>,
    stockMovements: List<StockMovement>,
    currentUser: UserAccount,
    onAdjustStock: (productId: Long, type: String, amount: Double, reason: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Status Stok, 1: Riwayat Mutasi
    var filterLowOnly by remember { mutableStateOf(false) }
    var adjustProduct by remember { mutableStateOf<Product?>(null) }
    var adjustType by remember { mutableStateOf("IN") } // "IN", "OUT", "ADJUSTMENT"

    val displayedProducts = remember(products, filterLowOnly) {
        if (filterLowOnly) products.filter { it.stock <= it.minStock } else products
    }

    val lowStockCount = products.count { it.stock <= it.minStock }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manajemen & Riwayat Stok",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Kelola stok masuk, stok keluar, opname, dan pantau stok menipis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Status Stok (${products.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Mutasi (${stockMovements.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                FilterChip(
                    selected = !filterLowOnly,
                    onClick = { filterLowOnly = false },
                    label = { Text("Semua Produk") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = filterLowOnly,
                    onClick = { filterLowOnly = true },
                    label = { Text("Peringatan Stok Menipis ($lowStockCount)") },
                    leadingIcon = {
                        if (lowStockCount > 0) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error,
                        selectedLabelColor = Color.White
                    )
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedProducts) { prod ->
                    val isLow = prod.stock <= prod.minStock
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp).fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Min. Stok: ${UnitConverter.formatQty(prod.minStock)} ${prod.unit} • ${prod.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Stok: ${UnitConverter.formatQty(prod.stock)} ${prod.unit}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (isLow) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Quick adjustment buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        adjustProduct = prod
                                        adjustType = "IN"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Masuk", modifier = Modifier.size(16.dp))
                                    Text("Masuk", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        adjustProduct = prod
                                        adjustType = "OUT"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Keluar", modifier = Modifier.size(16.dp))
                                    Text("Keluar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Stock Movements Audit Trail
            if (stockMovements.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Belum ada riwayat mutasi stok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(stockMovements) { mov ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isAdd = mov.type == "IN" || mov.type == "CANCELLED_RESTORE"
                                    Icon(
                                        imageVector = if (isAdd) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (isAdd) EmeraldPrimary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(mov.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${mov.reason} • ${CurrencyFormatter.formatDate(mov.timestamp)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val prefix = if (mov.quantity > 0 && mov.type != "ADJUSTMENT") "+" else ""
                                    Text(
                                        text = "$prefix${UnitConverter.formatQty(mov.quantity)} ${mov.unit}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (mov.type == "IN" || mov.type == "CANCELLED_RESTORE") EmeraldPrimary else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Sisa: ${UnitConverter.formatQty(mov.newStock)} ${mov.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Adjust Stock Dialog
        adjustProduct?.let { prod ->
            var amountInput by remember { mutableStateOf("1") }
            var reasonInput by remember { mutableStateOf(if (adjustType == "IN") "Kulakan / Stok Masuk" else "Barang Rusak / Kadaluwarsa") }

            AlertDialog(
                onDismissRequest = { adjustProduct = null },
                title = {
                    Text(
                        text = when (adjustType) {
                            "IN" -> "Stok Masuk: ${prod.name}"
                            "OUT" -> "Stok Keluar: ${prod.name}"
                            else -> "Penyesuaian Stok: ${prod.name}"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Stok saat ini: ${UnitConverter.formatQty(prod.stock)} ${prod.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Jumlah (${prod.unit})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = reasonInput,
                            onValueChange = { reasonInput = it },
                            label = { Text("Alasan / Catatan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onAdjustStock(prod.id, adjustType, amount, reasonInput)
                                adjustProduct = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { adjustProduct = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
