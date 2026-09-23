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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.UserAccount
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@Composable
fun ProductManagementScreen(
    products: List<Product>,
    categories: List<Category>,
    currentUser: UserAccount,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val filtered = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            (selectedCategory == "Semua" || p.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             p.name.contains(searchQuery, ignoreCase = true) ||
             p.sku.contains(searchQuery, ignoreCase = true) ||
             p.barcode.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Manajemen Produk",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kelola katalog, harga modal/jual, satuan, dan stok",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, SKU, atau barcode...") },
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

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "Semua",
                        onClick = { selectedCategory = "Semua" },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory.equals(cat.name, ignoreCase = true),
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Product Items List
            if (filtered.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    Text(
                        text = "Tidak ada produk yang sesuai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filtered) { prod ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!prod.isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "Nonaktif",
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${prod.category} • SKU: ${prod.sku.ifBlank { "-" }} • Barcode: ${prod.barcode.ifBlank { "-" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(
                                            text = "Jual: ${CurrencyFormatter.format(prod.sellingPrice)}/${prod.unit}",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Modal: ${CurrencyFormatter.format(prod.costPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Stok: ${UnitConverter.formatQty(prod.stock)} ${prod.unit}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isLow) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    if (currentUser.role == "ADMIN") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            IconButton(
                                                onClick = { productToEdit = prod },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { productToDelete = prod },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Product (Admin Only)
        if (currentUser.role == "ADMIN") {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }

        // Add / Edit Product Dialog
        if (showAddDialog || productToEdit != null) {
            ProductEditDialog(
                product = productToEdit,
                categories = categories,
                onDismiss = {
                    showAddDialog = false
                    productToEdit = null
                },
                onSave = { savedProd ->
                    onSaveProduct(savedProd)
                    showAddDialog = false
                    productToEdit = null
                }
            )
        }

        // Delete confirmation
        productToDelete?.let { prod ->
            ConfirmationDialog(
                title = "Hapus Produk",
                message = "Apakah Anda yakin ingin menghapus '${prod.name}'? Produk ini akan dihapus dari katalog.",
                isDestructive = true,
                onConfirm = {
                    onDeleteProduct(prod)
                    productToDelete = null
                },
                onDismiss = { productToDelete = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEditDialog(
    product: Product?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var category by remember { mutableStateOf(product?.category ?: (categories.firstOrNull()?.name ?: "Sembako")) }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toInt()?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(if (product != null) UnitConverter.formatQty(product.stock) else "10") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }
    var minStock by remember { mutableStateOf(if (product != null) UnitConverter.formatQty(product.minStock) else "5") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var isActive by remember { mutableStateOf(product?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Tambah Produk Baru" else "Edit Produk",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Kode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                // Category Selection
                Text("Kategori:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat.name,
                            onClick = { category = cat.name },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Prices
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Harga Modal") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Harga Jual *") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                // Unit Selector
                Text("Satuan Penjualan:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    UnitConverter.ALL_UNITS.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Stock & Min Stock
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stok Awal ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Stok Min.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Active switch
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Produk Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newOrUpdated = (product ?: Product(name = name)).copy(
                            name = name.trim(),
                            sku = sku.trim(),
                            barcode = barcode.trim(),
                            category = category,
                            costPrice = costPrice.replace(".", "").toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.replace(".", "").toDoubleOrNull() ?: 0.0,
                            stock = stock.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            minStock = minStock.replace(',', '.').toDoubleOrNull() ?: 5.0,
                            description = description.trim(),
                            isActive = isActive
                        )
                        onSave(newOrUpdated)
                    }
                },
                enabled = name.isNotBlank() && (sellingPrice.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
