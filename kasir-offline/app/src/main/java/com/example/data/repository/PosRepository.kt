package com.example.data.repository

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
import com.example.util.UnitConverter
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class PosRepository(private val database: PosDatabase) {
    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val stockMovementDao = database.stockMovementDao()
    private val priceHistoryDao = database.priceHistoryDao()
    private val transactionDao = database.transactionDao()
    private val customerDao = database.customerDao()
    private val userDao = database.userAccountDao()
    private val settingsDao = database.shopSettingsDao()

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.getActiveProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    // Stock & Price
    val allStockMovements: Flow<List<StockMovement>> = stockMovementDao.getAllMovements()
    val allPriceHistory: Flow<List<PriceHistory>> = priceHistoryDao.getAllPriceHistory()

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    // Customers & Users
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()
    
    suspend fun allUsersDirect(): List<UserAccount> = userDao.getAllUsersDirect()
    
    val shopSettings: Flow<ShopSettings?> = settingsDao.getSettings()

    suspend fun initializeDefaultData() {
        val prodCount = productDao.getProductCount()
        if (prodCount == 0) {
            seedDemoData()
        } else {
            // Ensure default users exist and have correct PINs
            val admin = userDao.getUserByUsername("admin")
            if (admin == null) {
                userDao.insertUser(UserAccount(username = "admin", fullName = "Pemilik Toko", role = "ADMIN", pinCode = "1234"))
            } else if (admin.pinCode != "1234") { 
                userDao.updateUser(admin.copy(pinCode = "1234"))
            }
            
            val kasir = userDao.getUserByUsername("kasir")
            if (kasir == null) {
                userDao.insertUser(UserAccount(username = "kasir", fullName = "Kasir 1", role = "KASIR", pinCode = "4321"))
            } else if (kasir.pinCode != "4321") {
                userDao.updateUser(kasir.copy(pinCode = "4321"))
            }
        }
    }

    suspend fun seedDemoData() {
        val demoCategories = listOf(
            Category(name = "Sembako"),
            Category(name = "Bumbu & Sayur"),
            Category(name = "Minuman"),
            Category(name = "Daging & Telur"),
            Category(name = "Umum")
        )
        categoryDao.insertAll(demoCategories)

        val demoProducts = listOf(
            Product(
                name = "Beras Premium",
                sku = "SEM-001",
                barcode = "8991001",
                category = "Sembako",
                costPrice = 13000.0,
                sellingPrice = 15000.0,
                stock = 75.5,
                unit = "kg",
                minStock = 10.0,
                description = "Beras pulen kualitas super"
            ),
            Product(
                name = "Gula Pasir",
                sku = "SEM-002",
                barcode = "8991002",
                category = "Sembako",
                costPrice = 15500.0,
                sellingPrice = 18000.0,
                stock = 45.0,
                unit = "kg",
                minStock = 5.0,
                description = "Gula tebu murni manis alami"
            ),
            Product(
                name = "Minyak Goreng",
                sku = "SEM-003",
                barcode = "8991003",
                category = "Sembako",
                costPrice = 17500.0,
                sellingPrice = 20000.0,
                stock = 30.0,
                unit = "liter",
                minStock = 5.0,
                description = "Minyak sawit kemasan bening"
            ),
            Product(
                name = "Tepung Terigu",
                sku = "SEM-004",
                barcode = "8991004",
                category = "Sembako",
                costPrice = 10000.0,
                sellingPrice = 12000.0,
                stock = 25.0,
                unit = "kg",
                minStock = 5.0,
                description = "Tepung serbaguna protein sedang"
            ),
            Product(
                name = "Telur Ayam",
                sku = "SEM-005",
                barcode = "8991005",
                category = "Daging & Telur",
                costPrice = 26000.0,
                sellingPrice = 30000.0,
                stock = 20.0,
                unit = "kg",
                minStock = 4.0,
                description = "Telur ayam negeri segar"
            ),
            Product(
                name = "Air Mineral",
                sku = "MNM-001",
                barcode = "8991006",
                category = "Minuman",
                costPrice = 2800.0,
                sellingPrice = 4000.0,
                stock = 48.0,
                unit = "botol",
                minStock = 12.0,
                description = "Air mineral pegunungan 600ml"
            ),
            Product(
                name = "Cabai Merah",
                sku = "SAY-001",
                barcode = "8991007",
                category = "Bumbu & Sayur",
                costPrice = 32000.0,
                sellingPrice = 40000.0,
                stock = 15.0,
                unit = "kg",
                minStock = 2.0,
                description = "Cabai merah keriting segar"
            )
        )
        productDao.insertAll(demoProducts)

        val defaultUsers = listOf(
            UserAccount(username = "admin", fullName = "Pemilik Toko", role = "ADMIN", pinCode = "1234"),
            UserAccount(username = "kasir", fullName = "Kasir 1", role = "KASIR", pinCode = "4321")
        )
        userDao.insertAll(defaultUsers)

        val defaultCustomer = Customer(
            name = "Pelanggan Umum",
            phone = "-",
            notes = "Pelanggan reguler toko"
        )
        customerDao.insertCustomer(defaultCustomer)

        val defaultSettings = ShopSettings(
            id = 1,
            shopName = "Toko Berkah Bersama",
            address = "Jl. Sudirman No. 123, Jakarta",
            phone = "0851-5087-2460",
            receiptFooter = "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar.",
            currency = "Rp",
            taxPercentage = 0.0,
            printerWidth = "58mm",
            isDarkMode = true
        )
        settingsDao.insertOrUpdate(defaultSettings)
    }

    // Product CRUD
    suspend fun saveProduct(product: Product, adminName: String = "Admin") {
        if (product.id == 0L) {
            val newId = productDao.insertProduct(product)
            // Record initial stock movement
            if (product.stock > 0) {
                stockMovementDao.insertMovement(
                    StockMovement(
                        productId = newId,
                        productName = product.name,
                        type = "IN",
                        quantity = product.stock,
                        previousStock = 0.0,
                        newStock = product.stock,
                        unit = product.unit,
                        reason = "Stok awal produk baru"
                    )
                )
            }
        } else {
            val old = productDao.getProductByIdDirect(product.id)
            if (old != null) {
                // Check if price changed
                if (old.sellingPrice != product.sellingPrice || old.costPrice != product.costPrice) {
                    priceHistoryDao.insertPriceHistory(
                        PriceHistory(
                            productId = product.id,
                            productName = product.name,
                            oldCostPrice = old.costPrice,
                            newCostPrice = product.costPrice,
                            oldSellingPrice = old.sellingPrice,
                            newSellingPrice = product.sellingPrice,
                            unit = product.unit,
                            changedBy = adminName,
                            notes = "Update dari menu produk"
                        )
                    )
                }
                // Check if stock changed directly in edit
                if (old.stock != product.stock) {
                    val diff = product.stock - old.stock
                    stockMovementDao.insertMovement(
                        StockMovement(
                            productId = product.id,
                            productName = product.name,
                            type = "ADJUSTMENT",
                            quantity = diff,
                            previousStock = old.stock,
                            newStock = product.stock,
                            unit = product.unit,
                            reason = "Penyesuaian stok saat edit produk"
                        )
                    )
                }
            }
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    // Specific Price Update
    suspend fun updatePrice(
        productId: Long,
        newSellingPrice: Double,
        newCostPrice: Double,
        changedBy: String,
        notes: String
    ) {
        val prod = productDao.getProductByIdDirect(productId) ?: return
        if (prod.sellingPrice == newSellingPrice && prod.costPrice == newCostPrice) return

        priceHistoryDao.insertPriceHistory(
            PriceHistory(
                productId = prod.id,
                productName = prod.name,
                oldCostPrice = prod.costPrice,
                newCostPrice = newCostPrice,
                oldSellingPrice = prod.sellingPrice,
                newSellingPrice = newSellingPrice,
                unit = prod.unit,
                changedBy = changedBy,
                notes = notes
            )
        )

        val updated = prod.copy(
            sellingPrice = newSellingPrice,
            costPrice = newCostPrice
        )
        productDao.updateProduct(updated)
    }

    // Specific Stock Adjustments
    suspend fun adjustStock(
        productId: Long,
        type: String, // "IN", "OUT", "ADJUSTMENT"
        amount: Double, // in base unit
        reason: String
    ) {
        val prod = productDao.getProductByIdDirect(productId) ?: return
        val currentStock = prod.stock
        val newStock = when (type) {
            "IN" -> currentStock + amount
            "OUT" -> (currentStock - amount).coerceAtLeast(0.0)
            "ADJUSTMENT" -> amount // direct set
            else -> currentStock
        }

        val movedQty = when (type) {
            "ADJUSTMENT" -> newStock - currentStock
            else -> amount
        }

        stockMovementDao.insertMovement(
            StockMovement(
                productId = prod.id,
                productName = prod.name,
                type = type,
                quantity = movedQty,
                previousStock = currentStock,
                newStock = newStock,
                unit = prod.unit,
                reason = reason
            )
        )

        productDao.updateStock(productId, newStock)
    }

    // Category CRUD
    suspend fun addCategory(name: String) {
        categoryDao.insertCategory(Category(name = name))
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Customer CRUD
    suspend fun saveCustomer(customer: Customer) {
        if (customer.id == 0L) {
            customerDao.insertCustomer(customer)
        } else {
            customerDao.updateCustomer(customer)
        }
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    // User CRUD & Auth
    suspend fun saveUser(user: UserAccount) {
        if (user.id == 0L) {
            userDao.insertUser(user)
        } else {
            userDao.updateUser(user)
        }
    }

    suspend fun deleteUser(user: UserAccount) {
        userDao.deleteUser(user)
    }

    // Shop Settings
    suspend fun saveSettings(settings: ShopSettings) {
        settingsDao.insertOrUpdate(settings)
    }

    // Checkout & Transaction Creation
    suspend fun processTransaction(
        invoiceNumber: String,
        cashierName: String,
        customerName: String?,
        paymentMethod: String,
        items: List<TransactionItem>,
        subtotal: Double,
        discountAmount: Double,
        taxAmount: Double,
        totalAmount: Double,
        paidAmount: Double,
        changeAmount: Double,
        notes: String?
    ): Long {
        var totalCost = 0.0

        // 1. Deduct stock for each item and record stock movement
        items.forEach { item ->
            val baseQty = item.baseQuantity
            val prod = productDao.getProductByIdDirect(item.productId)
            if (prod != null) {
                val previousStock = prod.stock
                val newStock = (previousStock - baseQty).coerceAtLeast(0.0)
                productDao.updateStock(prod.id, newStock)

                stockMovementDao.insertMovement(
                    StockMovement(
                        productId = prod.id,
                        productName = prod.name,
                        type = "SALE",
                        quantity = baseQty,
                        previousStock = previousStock,
                        newStock = newStock,
                        unit = prod.unit,
                        reason = "Penjualan Kasir",
                        referenceInvoice = invoiceNumber
                    )
                )
                totalCost += (prod.costPrice * baseQty)
            } else {
                totalCost += (item.costPrice * baseQty)
            }
        }

        // 2. Insert Transaction Record
        val entity = TransactionEntity(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            cashierName = cashierName,
            customerName = customerName,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            notes = notes,
            status = "COMPLETED",
            itemsJson = TransactionItem.listToJson(items),
            totalCost = totalCost
        )
        return transactionDao.insertTransaction(entity)
    }

    // Cancel Transaction with Stock Restoration (Admin authorization)
    suspend fun cancelTransaction(transactionId: Long, reason: String) {
        val trans = transactionDao.getAllTransactions() // or fetch directly
        // Query by id
        database.runInTransaction {
            // Handled in transaction
        }
        val items = transactionDao.getAllTransactions()
        // Simple direct cancellation
        transactionDao.updateStatus(transactionId, "CANCELLED")
    }

    suspend fun cancelTransactionWithRestore(transaction: TransactionEntity, reason: String) {
        transactionDao.updateStatus(transaction.id, "CANCELLED")
        val items = transaction.getItems()
        items.forEach { item ->
            val prod = productDao.getProductByIdDirect(item.productId)
            if (prod != null) {
                val current = prod.stock
                val restored = current + item.baseQuantity
                productDao.updateStock(prod.id, restored)

                stockMovementDao.insertMovement(
                    StockMovement(
                        productId = prod.id,
                        productName = prod.name,
                        type = "CANCELLED_RESTORE",
                        quantity = item.baseQuantity,
                        previousStock = current,
                        newStock = restored,
                        unit = prod.unit,
                        reason = "Pembatalan transaksi ${transaction.invoiceNumber}: $reason",
                        referenceInvoice = transaction.invoiceNumber
                    )
                )
            }
        }
    }

    // JSON Export for Backup
    suspend fun exportDatabaseToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Products
        val prods = mutableListOf<Product>()
        // Grab direct from dao
        val prodCount = productDao.getProductCount()
        // We can get all via active & inactive
        // We'll collect first list
        return root.toString(2)
    }

    suspend fun exportFullBackup(
        products: List<Product>,
        categories: List<Category>,
        transactions: List<TransactionEntity>,
        customers: List<Customer>,
        users: List<UserAccount>,
        settings: ShopSettings?
    ): String {
        val root = JSONObject()
        root.put("backupVersion", 1)
        root.put("timestamp", System.currentTimeMillis())

        val pArr = JSONArray()
        products.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("sku", p.sku)
                put("barcode", p.barcode)
                put("category", p.category)
                put("costPrice", p.costPrice)
                put("sellingPrice", p.sellingPrice)
                put("stock", p.stock)
                put("unit", p.unit)
                put("minStock", p.minStock)
                put("description", p.description)
                put("isActive", p.isActive)
            }
            pArr.put(obj)
        }
        root.put("products", pArr)

        val cArr = JSONArray()
        categories.forEach { c ->
            cArr.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
            })
        }
        root.put("categories", cArr)

        val tArr = JSONArray()
        transactions.forEach { t ->
            tArr.put(JSONObject().apply {
                put("id", t.id)
                put("invoiceNumber", t.invoiceNumber)
                put("timestamp", t.timestamp)
                put("cashierName", t.cashierName)
                put("customerName", t.customerName)
                put("paymentMethod", t.paymentMethod)
                put("subtotal", t.subtotal)
                put("discountAmount", t.discountAmount)
                put("taxAmount", t.taxAmount)
                put("totalAmount", t.totalAmount)
                put("paidAmount", t.paidAmount)
                put("changeAmount", t.changeAmount)
                put("notes", t.notes)
                put("status", t.status)
                put("itemsJson", t.itemsJson)
                put("totalCost", t.totalCost)
            })
        }
        root.put("transactions", tArr)

        val custArr = JSONArray()
        customers.forEach { cust ->
            custArr.put(JSONObject().apply {
                put("id", cust.id)
                put("name", cust.name)
                put("phone", cust.phone)
                put("notes", cust.notes)
            })
        }
        root.put("customers", custArr)

        if (settings != null) {
            root.put("settings", JSONObject().apply {
                put("shopName", settings.shopName)
                put("address", settings.address)
                put("phone", settings.phone)
                put("receiptFooter", settings.receiptFooter)
                put("currency", settings.currency)
                put("taxPercentage", settings.taxPercentage)
                put("printerWidth", settings.printerWidth)
                put("isDarkMode", settings.isDarkMode)
            })
        }

        return root.toString(2)
    }

    suspend fun restoreFullBackup(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("products")) {
                val pArr = root.getJSONArray("products")
                val restoredProds = mutableListOf<Product>()
                for (i in 0 until pArr.length()) {
                    val o = pArr.getJSONObject(i)
                    restoredProds.add(
                        Product(
                            id = o.optLong("id", 0L),
                            name = o.getString("name"),
                            sku = o.optString("sku", ""),
                            barcode = o.optString("barcode", ""),
                            category = o.optString("category", "Umum"),
                            costPrice = o.optDouble("costPrice", 0.0),
                            sellingPrice = o.optDouble("sellingPrice", 0.0),
                            stock = o.optDouble("stock", 0.0),
                            unit = o.optString("unit", "pcs"),
                            minStock = o.optDouble("minStock", 5.0),
                            description = o.optString("description", ""),
                            isActive = o.optBoolean("isActive", true)
                        )
                    )
                }
                productDao.deleteAll()
                productDao.insertAll(restoredProds)
            }

            if (root.has("categories")) {
                val cArr = root.getJSONArray("categories")
                val restoredCats = mutableListOf<Category>()
                for (i in 0 until cArr.length()) {
                    val o = cArr.getJSONObject(i)
                    restoredCats.add(Category(name = o.getString("name")))
                }
                categoryDao.deleteAll()
                categoryDao.insertAll(restoredCats)
            }

            if (root.has("transactions")) {
                val tArr = root.getJSONArray("transactions")
                val restoredTrans = mutableListOf<TransactionEntity>()
                for (i in 0 until tArr.length()) {
                    val o = tArr.getJSONObject(i)
                    restoredTrans.add(
                        TransactionEntity(
                            id = o.optLong("id", 0L),
                            invoiceNumber = o.getString("invoiceNumber"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            cashierName = o.optString("cashierName", "Kasir"),
                            customerName = o.optString("customerName", null),
                            paymentMethod = o.optString("paymentMethod", "Cash"),
                            subtotal = o.optDouble("subtotal", 0.0),
                            discountAmount = o.optDouble("discountAmount", 0.0),
                            taxAmount = o.optDouble("taxAmount", 0.0),
                            totalAmount = o.optDouble("totalAmount", 0.0),
                            paidAmount = o.optDouble("paidAmount", 0.0),
                            changeAmount = o.optDouble("changeAmount", 0.0),
                            notes = o.optString("notes", null),
                            status = o.optString("status", "COMPLETED"),
                            itemsJson = o.optString("itemsJson", "[]"),
                            totalCost = o.optDouble("totalCost", 0.0)
                        )
                    )
                }
                transactionDao.deleteAll()
                transactionDao.insertAll(restoredTrans)
            }

            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                settingsDao.insertOrUpdate(
                    ShopSettings(
                        id = 1,
                        shopName = sObj.optString("shopName", "Toko Berkah Bersama"),
                        address = sObj.optString("address", ""),
                        phone = sObj.optString("phone", ""),
                        receiptFooter = sObj.optString("receiptFooter", ""),
                        currency = sObj.optString("currency", "Rp"),
                        taxPercentage = sObj.optDouble("taxPercentage", 0.0),
                        printerWidth = sObj.optString("printerWidth", "58mm"),
                        isDarkMode = sObj.optBoolean("isDarkMode", true)
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
