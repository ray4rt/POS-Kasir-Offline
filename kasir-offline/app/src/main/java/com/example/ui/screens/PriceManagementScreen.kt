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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.UserAccount
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceManagementScreen(
    products: List<Product>,
    priceHistoryList: List<PriceHistory>,
    currentUser: UserAccount,
    onUpdatePrice: (productId: Long, newSelling: Double, newCost: Double, notes: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daftar Harga, 1: Riwayat Perubahan
    var searchQuery by remember { mutableStateOf("") }
    var productForPriceEdit by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(products, searchQuery) {
        products.filter { p ->
            searchQuery.isBlank() ||
            p.name.contains(searchQuery, ignoreCase = true) ||
            p.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manajemen Harga",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Atur harga jual, harga modal, pantau margin & riwayat perubahan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Daftar Harga Produk") },
                icon = { Icon(Icons.Default.PriceChange, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Perubahan (${priceHistoryList.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari produk...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { prod ->
                    val profitMargin = if (prod.sellingPrice > 0) {
                        ((prod.sellingPrice - prod.costPrice) / prod.sellingPrice) * 100
                    } else 0.0

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
                                    text = "Satuan: ${prod.unit} • ${prod.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Harga Modal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(CurrencyFormatter.format(prod.costPrice), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("Harga Jual", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(CurrencyFormatter.format(prod.sellingPrice), fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("Margin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${profitMargin.toInt()}%", fontWeight = FontWeight.Bold, color = if (profitMargin >= 20) EmeraldPrimary else Color(0xFFF59E0B), fontSize = 13.sp)
                                    }
                                }
                            }

                            Button(
                                onClick = { productForPriceEdit = prod },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ubah", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Price History Audit Log List
            if (priceHistoryList.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Belum ada riwayat perubahan harga.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(priceHistoryList) { history ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = history.productName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatDate(history.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Jual: ${CurrencyFormatter.format(history.oldSellingPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = androidx.compose.ui.Modifier.size(12.dp))
                                    Text(
                                        text = CurrencyFormatter.format(history.newSellingPrice),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                                Text(
                                    text = "Diubah oleh: ${history.changedBy}${if (history.notes.isNotBlank()) " • Catatan: ${history.notes}" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Price Update Modal
        productForPriceEdit?.let { prod ->
            var newSellingInput by remember { mutableStateOf(prod.sellingPrice.toInt().toString()) }
            var newCostInput by remember { mutableStateOf(prod.costPrice.toInt().toString()) }
            var notesInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { productForPriceEdit = null },
                title = {
                    Text("Ubah Harga: ${prod.name}", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Satuan: ${prod.unit} • Harga Sekarang: ${CurrencyFormatter.format(prod.sellingPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = newSellingInput,
                            onValueChange = { newSellingInput = it },
                            label = { Text("Harga Jual Baru") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = newCostInput,
                            onValueChange = { newCostInput = it },
                            label = { Text("Harga Modal Baru") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Alasan Perubahan (Opsional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newSelling = newSellingInput.replace(".", "").toDoubleOrNull() ?: prod.sellingPrice
                            val newCost = newCostInput.replace(".", "").toDoubleOrNull() ?: prod.costPrice
                            onUpdatePrice(prod.id, newSelling, newCost, notesInput)
                            productForPriceEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan Perubahan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productForPriceEdit = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
