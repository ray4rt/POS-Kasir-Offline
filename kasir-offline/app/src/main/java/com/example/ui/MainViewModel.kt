package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.PosDatabase
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.data.model.StockMovement
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItem
import com.example.data.model.UserAccount
import com.example.data.repository.PosRepository
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen(val label: String, val icon: String, val adminOnly: Boolean = false) {
    DASHBOARD("Dashboard", "dashboard"),
    CASHIER("Kasir", "point_of_sale"),
    PRODUCTS("Produk", "inventory_2"),
    CATEGORIES("Kategori", "category", true),
    STOCK("Stok", "warehouse"),
    PRICE_MANAGEMENT("Manajemen Harga", "price_change", true),
    TRANSACTIONS("Transaksi", "receipt_long"),
    CUSTOMERS("Pelanggan", "people"),
    REPORTS("Laporan", "analytics", true),
    USERS("Pengguna", "manage_accounts", true),
    BACKUP_RESTORE("Backup & Restore", "backup", true),
    SETTINGS("Pengaturan", "settings", true),
    ABOUT_DONATE("Tentang & Donasi", "volunteer_activism")
}

data class CartItem(
    val product: Product,
    val quantity: Double,
    val selectedUnit: String,
    val discountAmount: Double = 0.0
) {
    val baseQuantity: Double
        get() = UnitConverter.convertToBaseUnitQty(quantity, selectedUnit, product.unit)

    val itemTotal: Double
        get() = UnitConverter.calculateItemTotal(
            basePrice = product.sellingPrice,
            inputQty = quantity,
            inputUnit = selectedUnit,
            baseUnit = product.unit,
            discountPerItem = discountAmount
        )

    fun toTransactionItem(): TransactionItem {
        return TransactionItem(
            productId = product.id,
            productName = product.name,
            baseUnit = product.unit,
            selectedUnit = selectedUnit,
            quantity = quantity,
            baseQuantity = baseQuantity,
            unitPrice = product.sellingPrice,
            discountAmount = discountAmount,
            subtotal = itemTotal,
            costPrice = product.costPrice
        )
    }
}

data class ParkedCart(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<CartItem>,
    val discount: Double,
    val notes: String?
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PosDatabase.getDatabase(application)
    val repository = PosRepository(database)

    // Login State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Current navigation screen
    private val _currentScreen = MutableStateFlow(AppScreen.CASHIER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Current Active User
    private val _currentUser = MutableStateFlow(
        UserAccount(id = 1, username = "admin", fullName = "Pemilik Toko", role = "ADMIN", pinCode = "1234")
    )
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    // Toast notification
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Dark Mode override
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Data streams from repository
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockMovements: StateFlow<List<StockMovement>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPriceHistory: StateFlow<List<PriceHistory>> = repository.allPriceHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shopSettings: StateFlow<ShopSettings?> = repository.shopSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Kasir Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()

    private val _applyTax = MutableStateFlow(false)
    val applyTax: StateFlow<Boolean> = _applyTax.asStateFlow()

    private val _cartNotes = MutableStateFlow("")
    val cartNotes: StateFlow<String> = _cartNotes.asStateFlow()

    private val _parkedCarts = MutableStateFlow<List<ParkedCart>>(emptyList())
    val parkedCarts: StateFlow<List<ParkedCart>> = _parkedCarts.asStateFlow()

    // Completed transaction for receipt dialog
    private val _lastCompletedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val lastCompletedTransaction: StateFlow<TransactionEntity?> = _lastCompletedTransaction.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultData()
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen.adminOnly && currentUser.value.role != "ADMIN") {
            showToast("Halaman ini memerlukan akses Admin!")
            return
        }
        _currentScreen.value = screen
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        viewModelScope.launch {
            val current = shopSettings.value ?: ShopSettings()
            repository.saveSettings(current.copy(isDarkMode = _isDarkMode.value))
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun login(role: String, pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (role == "RESET_INIT") {
                repository.initializeDefaultData()
                showToast("PIN telah direset ke pengaturan awal.")
                onResult(false)
                return@launch
            }

            // Try to find in the current list first (fast)
            val currentUsers = allUsers.value
            var user = currentUsers.find { it.role.equals(role, ignoreCase = true) && it.pinCode == pin }
            
            // If not found, check database directly
            if (user == null) {
                val dbUsers = repository.allUsersDirect()
                user = dbUsers.find { it.role.equals(role, ignoreCase = true) && it.pinCode == pin }
            }

            if (user != null) {
                _currentUser.value = user
                _isLoggedIn.value = true
                showToast("Selamat datang, ${user.fullName}!")
                onResult(true)
            } else {
                showToast("PIN salah untuk akses $role!")
                onResult(false)
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentScreen.value = AppScreen.CASHIER
    }

    // Role / User Switch
    fun switchUser(user: UserAccount, pinAttempt: String, onSuccess: () -> Unit) {
        if (user.pinCode == pinAttempt) {
            _currentUser.value = user
            showToast("Login sebagai ${user.fullName} (${user.role})")
            onSuccess()
        } else {
            showToast("PIN salah! Silakan coba lagi.")
        }
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Double = 1.0, selectedUnit: String? = null) {
        val unit = selectedUnit ?: product.unit
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id && it.selectedUnit == unit }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = existing.quantity + quantity
            currentList[existingIndex] = existing.copy(quantity = newQty)
        } else {
            currentList.add(
                CartItem(
                    product = product,
                    quantity = quantity,
                    selectedUnit = unit
                )
            )
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQty(index: Int, newQty: Double) {
        val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            if (newQty <= 0.0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQty)
            }
            _cartItems.value = currentList
        }
    }

    fun updateCartItemUnit(index: Int, newUnit: String) {
        val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(selectedUnit = newUnit)
            _cartItems.value = currentList
        }
    }

    fun updateCartItemDiscount(index: Int, discount: Double) {
        val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(discountAmount = discount)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(index: Int) {
        val currentList = _cartItems.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0.0
        _cartNotes.value = ""
        _selectedCustomer.value = null
    }

    fun setCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setCartDiscount(discount: Double) {
        _cartDiscount.value = discount.coerceAtLeast(0.0)
    }

    fun setApplyTax(apply: Boolean) {
        _applyTax.value = apply
    }

    fun setCartNotes(notes: String) {
        _cartNotes.value = notes
    }

    // Hold / Park Cart
    fun holdCurrentCart() {
        if (_cartItems.value.isEmpty()) {
            showToast("Keranjang kosong, tidak ada yang dapat disimpan.")
            return
        }
        val parked = ParkedCart(
            customerName = _selectedCustomer.value?.name,
            items = _cartItems.value,
            discount = _cartDiscount.value,
            notes = _cartNotes.value
        )
        _parkedCarts.value = _parkedCarts.value + parked
        clearCart()
        showToast("Transaksi disimpan (Hold # ${_parkedCarts.value.size})")
    }

    fun restoreParkedCart(parked: ParkedCart) {
        _cartItems.value = parked.items
        _cartDiscount.value = parked.discount
        _cartNotes.value = parked.notes ?: ""
        _selectedCustomer.value = allCustomers.value.find { it.name == parked.customerName }
        _parkedCarts.value = _parkedCarts.value.filter { it.id != parked.id }
        showToast("Transaksi berhasil dikembalikan ke keranjang")
    }

    fun removeParkedCart(parked: ParkedCart) {
        _parkedCarts.value = _parkedCarts.value.filter { it.id != parked.id }
    }

    // Checkout & Payment
    fun processCheckout(
        paymentMethod: String,
        paidAmount: Double,
        onSuccess: (TransactionEntity) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            showToast("Keranjang belanja masih kosong!")
            return
        }

        val subtotal = items.sumOf { it.itemTotal }
        val discount = _cartDiscount.value
        val taxRate = if (_applyTax.value) (shopSettings.value?.taxPercentage ?: 0.0) else 0.0
        val tax = ((subtotal - discount).coerceAtLeast(0.0) * (taxRate / 100.0))
        val total = (subtotal - discount + tax).coerceAtLeast(0.0)

        if (paidAmount < total && paymentMethod == "Cash") {
            showToast("Uang yang diterima kurang dari total tagihan!")
            return
        }

        val change = if (paymentMethod == "Cash") (paidAmount - total).coerceAtLeast(0.0) else 0.0
        val invoice = CurrencyFormatter.generateInvoiceNumber()
        val cashier = _currentUser.value.fullName
        val customer = _selectedCustomer.value?.name
        val notes = _cartNotes.value

        viewModelScope.launch {
            try {
                val transactionItems = items.map { it.toTransactionItem() }
                val newId = repository.processTransaction(
                    invoiceNumber = invoice,
                    cashierName = cashier,
                    customerName = customer,
                    paymentMethod = paymentMethod,
                    items = transactionItems,
                    subtotal = subtotal,
                    discountAmount = discount,
                    taxAmount = tax,
                    totalAmount = total,
                    paidAmount = paidAmount,
                    changeAmount = change,
                    notes = notes
                )

                val entity = TransactionEntity(
                    id = newId,
                    invoiceNumber = invoice,
                    timestamp = System.currentTimeMillis(),
                    cashierName = cashier,
                    customerName = customer,
                    paymentMethod = paymentMethod,
                    subtotal = subtotal,
                    discountAmount = discount,
                    taxAmount = tax,
                    totalAmount = total,
                    paidAmount = paidAmount,
                    changeAmount = change,
                    notes = notes,
                    status = "COMPLETED",
                    itemsJson = TransactionItem.listToJson(transactionItems)
                )

                _lastCompletedTransaction.value = entity
                clearCart()
                showToast("Transaksi $invoice berhasil!")
                onSuccess(entity)
            } catch (e: Exception) {
                showToast("Gagal memproses transaksi: ${e.localizedMessage}")
            }
        }
    }

    fun dismissReceipt() {
        _lastCompletedTransaction.value = null
    }

    // Cancel Transaction (with admin verify)
    fun cancelTransaction(transaction: TransactionEntity, reason: String) {
        if (_currentUser.value.role != "ADMIN") {
            showToast("Hanya Admin yang dapat membatalkan transaksi!")
            return
        }
        viewModelScope.launch {
            repository.cancelTransactionWithRestore(transaction, reason)
            showToast("Transaksi ${transaction.invoiceNumber} berhasil dibatalkan dan stok dikembalikan.")
        }
    }

    // Product Management
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product, _currentUser.value.fullName)
            showToast("Produk '${product.name}' berhasil disimpan.")
        }
    }

    fun deleteProduct(product: Product) {
        if (_currentUser.value.role != "ADMIN") {
            showToast("Hanya Admin yang dapat menghapus produk!")
            return
        }
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Produk '${product.name}' berhasil dihapus.")
        }
    }

    // Price Management
    fun updateProductPrice(
        productId: Long,
        newSellingPrice: Double,
        newCostPrice: Double,
        notes: String
    ) {
        if (_currentUser.value.role != "ADMIN") {
            showToast("Hanya Admin yang dapat mengubah harga!")
            return
        }
        viewModelScope.launch {
            repository.updatePrice(
                productId = productId,
                newSellingPrice = newSellingPrice,
                newCostPrice = newCostPrice,
                changedBy = _currentUser.value.fullName,
                notes = notes
            )
            showToast("Harga berhasil diperbarui dan dicatat di riwayat.")
        }
    }

    // Stock Management
    fun adjustStock(productId: Long, type: String, amount: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, type, amount, reason)
            showToast("Stok berhasil diperbarui.")
        }
    }

    // Category Management
    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name)
            showToast("Kategori '$name' berhasil ditambahkan.")
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showToast("Kategori '${category.name}' dihapus.")
        }
    }

    // Customer Management
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            showToast("Data pelanggan '${customer.name}' disimpan.")
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showToast("Pelanggan '${customer.name}' dihapus.")
        }
    }

    // User Management
    fun saveUser(user: UserAccount) {
        viewModelScope.launch {
            repository.saveUser(user)
            showToast("Pengguna '${user.username}' disimpan.")
        }
    }

    fun deleteUser(user: UserAccount) {
        viewModelScope.launch {
            repository.deleteUser(user)
            showToast("Pengguna '${user.username}' dihapus.")
        }
    }

    // Settings
    fun saveSettings(settings: ShopSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _isDarkMode.value = settings.isDarkMode
            showToast("Pengaturan toko berhasil diperbarui.")
        }
    }

    // Backup & Restore
    suspend fun getBackupJson(): String {
        return repository.exportFullBackup(
            products = allProducts.value,
            categories = allCategories.value,
            transactions = allTransactions.value,
            customers = allCustomers.value,
            users = allUsers.value,
            settings = shopSettings.value
        )
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreFullBackup(jsonString)
            if (success) {
                showToast("Database berhasil dipulihkan dari backup JSON!")
            } else {
                showToast("Format backup JSON tidak valid atau gagal dipulihkan.")
            }
            onResult(success)
        }
    }

    fun resetToDemo() {
        viewModelScope.launch {
            repository.seedDemoData()
            showToast("Data demo berhasil dimuat ulang.")
        }
    }
}
