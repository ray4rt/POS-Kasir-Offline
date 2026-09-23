package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.ui.CartItem
import com.example.ui.ParkedCart
import com.example.ui.components.PaymentDialog
import com.example.ui.components.WeightQtyDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    products: List<Product>,
    categories: List<Category>,
    customers: List<Customer>,
    settings: ShopSettings?,
    cartItems: List<CartItem>,
    selectedCustomer: Customer?,
    cartDiscount: Double,
    applyTax: Boolean,
    cartNotes: String,
    parkedCarts: List<ParkedCart>,
    onAddToCart: (Product, Double, String?) -> Unit,
    onUpdateCartQty: (Int, Double) -> Unit,
    onUpdateCartUnit: (Int, String) -> Unit,
    onUpdateCartDiscount: (Int, Double) -> Unit,
    onRemoveCartItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onSetCartDiscount: (Double) -> Unit,
    onSetApplyTax: (Boolean) -> Unit,
    onSetCartNotes: (String) -> Unit,
    onHoldCart: () -> Unit,
    onRestoreCart: (ParkedCart) -> Unit,
    onCheckoutConfirm: (method: String, paid: Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedProductForQty by remember { mutableStateOf<Product?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showMobileCartSheet by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showParkedCartsDialog by remember { mutableStateOf(false) }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            p.isActive &&
            (selectedCategory == "Semua" || p.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             p.name.contains(searchQuery, ignoreCase = true) ||
             p.barcode.contains(searchQuery, ignoreCase = true) ||
             p.sku.contains(searchQuery, ignoreCase = true))
        }
    }

    // Totals calculations
    val subtotal = cartItems.sumOf { it.itemTotal }
    val taxRate = if (applyTax) (settings?.taxPercentage ?: 0.0) else 0.0
    val taxAmount = ((subtotal - cartDiscount).coerceAtLeast(0.0) * (taxRate / 100.0))
    val grandTotal = (subtotal - cartDiscount + taxAmount).coerceAtLeast(0.0)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Main Area: Search, Categories, and Products Catalog
            Column(
                modifier = Modifier
                    .weight(if (isWideScreen) 1.2f else 1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Bar and Barcode Quick Scan Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari produk atau barcode...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            // If barcode matches exactly 1 product, quick add it
                            val match = products.find { it.barcode.equals(searchQuery.trim(), ignoreCase = true) }
                            if (match != null) {
                                onAddToCart(match, 1.0, null)
                                searchQuery = ""
                            }
                        }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_search_input")
                    )

                    // Hold / Parked Carts indicator button
                    if (parkedCarts.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showParkedCartsDialog = true },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .size(50.dp)
                        ) {
                            BadgedBox(badge = { Badge { Text("${parkedCarts.size}") } }) {
                                Icon(Icons.Default.PauseCircle, contentDescription = "Hold Transaksi", tint = EmeraldPrimary)
                            }
                        }
                    }
                }

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                    items(categories.size) { index ->
                        val cat = categories[index]
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

                // Products Grid
                if (filteredProducts.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Produk tidak ditemukan.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isWideScreen) 160.dp else 140.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        items(filteredProducts) { prod ->
                            PosProductCard(
                                product = prod,
                                onClick = {
                                    // For products with weight/volume units or if user wants to specify weight, open dialog
                                    val isDecimalProduct = UnitConverter.getUnitType(prod.unit) != com.example.util.UnitType.DISCRETE
                                    if (isDecimalProduct) {
                                        selectedProductForQty = prod
                                    } else {
                                        onAddToCart(prod, 1.0, prod.unit)
                                    }
                                },
                                onLongClick = {
                                    selectedProductForQty = prod
                                }
                            )
                        }
                    }
                }

                // Mobile Bottom Floating Cart Summary Bar
                if (!isWideScreen && cartItems.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showMobileCartSheet = true }
                            .testTag("mobile_cart_bar")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(badge = { Badge(containerColor = Color.White) { Text("${cartItems.size}", color = EmeraldPrimary) } }) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${cartItems.size} Item di Keranjang",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(grandTotal),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Button(
                                onClick = { showPaymentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bayar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Right Pane: Dedicated Shopping Cart on Desktop / Tablet (isWideScreen)
            if (isWideScreen) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .padding(start = 4.dp)
                ) {
                    CartContent(
                        cartItems = cartItems,
                        customers = customers,
                        selectedCustomer = selectedCustomer,
                        cartDiscount = cartDiscount,
                        applyTax = applyTax,
                        taxPercentage = settings?.taxPercentage ?: 0.0,
                        subtotal = subtotal,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        onUpdateQty = onUpdateCartQty,
                        onRemoveItem = onRemoveCartItem,
                        onClearCart = onClearCart,
                        onHoldCart = onHoldCart,
                        onSelectCustomerClick = { showCustomerPicker = true },
                        onSetDiscount = onSetCartDiscount,
                        onToggleTax = onSetApplyTax,
                        onPayClick = { showPaymentDialog = true }
                    )
                }
            }
        }

        // Weight & Quantity Dialog for decimal / fast input
        selectedProductForQty?.let { prod ->
            WeightQtyDialog(
                product = prod,
                onDismiss = { selectedProductForQty = null },
                onConfirm = { qty, unit ->
                    onAddToCart(prod, qty, unit)
                    selectedProductForQty = null
                }
            )
        }

        // Payment Dialog
        if (showPaymentDialog) {
            PaymentDialog(
                totalAmount = grandTotal,
                onDismiss = { showPaymentDialog = false },
                onConfirmPayment = { method, paid ->
                    showPaymentDialog = false
                    showMobileCartSheet = false
                    onCheckoutConfirm(method, paid)
                }
            )
        }

        // Mobile Bottom Sheet for Cart
        if (showMobileCartSheet && !isWideScreen) {
            ModalBottomSheet(
                onDismissRequest = { showMobileCartSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CartContent(
                    cartItems = cartItems,
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    cartDiscount = cartDiscount,
                    applyTax = applyTax,
                    taxPercentage = settings?.taxPercentage ?: 0.0,
                    subtotal = subtotal,
                    taxAmount = taxAmount,
                    grandTotal = grandTotal,
                    onUpdateQty = onUpdateCartQty,
                    onRemoveItem = onRemoveCartItem,
                    onClearCart = onClearCart,
                    onHoldCart = {
                        onHoldCart()
                        showMobileCartSheet = false
                    },
                    onSelectCustomerClick = { showCustomerPicker = true },
                    onSetDiscount = onSetCartDiscount,
                    onToggleTax = onSetApplyTax,
                    onPayClick = {
                        showPaymentDialog = true
                    }
                )
            }
        }

        // Customer Picker Dialog
        if (showCustomerPicker) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showCustomerPicker = false },
                title = { Text("Pilih Pelanggan", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCustomer(null)
                                        showCustomerPicker = false
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("Pelanggan Umum (Tanpa Nama)", fontWeight = FontWeight.Medium)
                            }
                            HorizontalDivider()
                        }
                        items(customers) { cust ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCustomer(cust)
                                        showCustomerPicker = false
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Column {
                                    Text(cust.name, fontWeight = FontWeight.SemiBold)
                                    if (cust.phone.isNotBlank()) {
                                        Text(cust.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCustomerPicker = false }) {
                        Text("Tutup")
                    }
                }
            )
        }

        // Parked Carts Dialog
        if (showParkedCartsDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showParkedCartsDialog = false },
                title = { Text("Transaksi Tersimpan (Hold)", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                        items(parkedCarts) { parked ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = parked.customerName ?: "Pelanggan Umum",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${parked.items.size} item • ${CurrencyFormatter.formatTimeOnly(parked.timestamp)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            onRestoreCart(parked)
                                            showParkedCartsDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Buka", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showParkedCartsDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}

@Composable
fun PosProductCard(
    product: Product,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isLowStock = product.stock <= product.minStock

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Category & Stock Indicator
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${UnitConverter.formatQty(product.stock)} ${product.unit}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price per unit
            Text(
                text = CurrencyFormatter.format(product.sellingPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
            )
            Text(
                text = "per ${product.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CartContent(
    cartItems: List<CartItem>,
    customers: List<Customer>,
    selectedCustomer: Customer?,
    cartDiscount: Double,
    applyTax: Boolean,
    taxPercentage: Double,
    subtotal: Double,
    taxAmount: Double,
    grandTotal: Double,
    onUpdateQty: (Int, Double) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onHoldCart: () -> Unit,
    onSelectCustomerClick: () -> Unit,
    onSetDiscount: (Double) -> Unit,
    onToggleTax: (Boolean) -> Unit,
    onPayClick: () -> Unit
) {
    var discountInput by remember { mutableStateOf(if (cartDiscount > 0) cartDiscount.toInt().toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cart Header & Customer Selector
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Keranjang Belanja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${cartItems.size} Jenis Produk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                if (cartItems.isNotEmpty()) {
                    IconButton(onClick = onHoldCart) {
                        Icon(Icons.Default.PauseCircle, contentDescription = "Simpan Sementara (Hold)", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onClearCart) {
                        Icon(Icons.Default.Delete, contentDescription = "Kosongkan Keranjang", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Customer selection chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onSelectCustomerClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedCustomer?.name ?: "Pelanggan Umum (Pilih)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        // Cart Items Scrollable List
        if (cartItems.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Text(
                    text = "Keranjang belanja kosong.\nPilih produk di sebelah kiri.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                itemsIndexed(cartItems) { index, item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.product.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveItem(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Qty modifier (+ / -)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                val step = if (item.selectedUnit == "kg" || item.selectedUnit == "liter") 0.25 else 1.0
                                                onUpdateQty(index, (item.quantity - step).coerceAtLeast(0.05))
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Text(
                                        text = "${UnitConverter.formatQty(item.quantity)} ${item.selectedUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                val step = if (item.selectedUnit == "kg" || item.selectedUnit == "liter") 0.25 else 1.0
                                                onUpdateQty(index, item.quantity + step)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                // Total for item
                                Text(
                                    text = CurrencyFormatter.format(item.itemTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Calculations & Payment Footer
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                Text(CurrencyFormatter.format(subtotal), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }

            // Transaction Discount input
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Text("Diskon (Rp)", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = {
                        discountInput = it
                        val d = it.toDoubleOrNull() ?: 0.0
                        onSetDiscount(d)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp).height(46.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
            }

            // Tax (PPN) Switch
            if (taxPercentage > 0.0) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pajak (${taxPercentage.toInt()}%)", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = applyTax,
                        onCheckedChange = onToggleTax,
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TOTAL AKHIR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.format(grandTotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPayClick,
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pay_action_button")
            ) {
                Text(
                    text = "BAYAR (F4)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
