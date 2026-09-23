package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ShopSettings
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DrawerContent
import com.example.ui.components.PosBottomNavigationBar
import com.example.ui.components.PosTopAppBar
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.components.UserSwitchDialog
import com.example.ui.screens.AboutDonateScreen
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.CashierScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PriceManagementScreen
import com.example.ui.screens.ProductManagementScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockManagementScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val lastCompletedTransaction by viewModel.lastCompletedTransaction.collectAsState()

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLogin = { role, pin, onResult ->
                viewModel.login(role, pin, onResult)
            }
        )
        return
    }

    val products by viewModel.allProducts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val stockMovements by viewModel.allStockMovements.collectAsState()
    val priceHistoryList by viewModel.allPriceHistory.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val settings by viewModel.shopSettings.collectAsState()

    // Cart state
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val cartDiscount by viewModel.cartDiscount.collectAsState()
    val applyTax by viewModel.applyTax.collectAsState()
    val cartNotes by viewModel.cartNotes.collectAsState()
    val parkedCarts by viewModel.parkedCarts.collectAsState()

    var showUserSwitchDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    lowStockCount = lowStockProducts.size,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    },
                    onLogout = {
                        showLogoutConfirmDialog = true
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                PosTopAppBar(
                    settings = settings,
                    currentUser = currentUser,
                    isDarkMode = isDarkMode,
                    cartItemCount = cartItems.size,
                    lowStockCount = lowStockProducts.size,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onUserSwitchClick = { showUserSwitchDialog = true },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onLogoutClick = { showLogoutConfirmDialog = true }
                )
            },
            bottomBar = {
                PosBottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onOpenMore = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            products = products,
                            transactions = transactions,
                            onViewAllTransactions = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                            onViewProductDetails = { viewModel.navigateTo(AppScreen.PRODUCTS) }
                        )
                    }
                    AppScreen.CASHIER -> {
                        CashierScreen(
                            products = products,
                            categories = categories,
                            customers = customers,
                            settings = settings,
                            cartItems = cartItems,
                            selectedCustomer = selectedCustomer,
                            cartDiscount = cartDiscount,
                            applyTax = applyTax,
                            cartNotes = cartNotes,
                            parkedCarts = parkedCarts,
                            onAddToCart = { prod, qty, unit -> viewModel.addToCart(prod, qty, unit) },
                            onUpdateCartQty = { idx, qty -> viewModel.updateCartItemQty(idx, qty) },
                            onUpdateCartUnit = { idx, unit -> viewModel.updateCartItemUnit(idx, unit) },
                            onUpdateCartDiscount = { idx, disc -> viewModel.updateCartItemDiscount(idx, disc) },
                            onRemoveCartItem = { idx -> viewModel.removeFromCart(idx) },
                            onClearCart = { viewModel.clearCart() },
                            onSelectCustomer = { cust -> viewModel.setCustomer(cust) },
                            onSetCartDiscount = { disc -> viewModel.setCartDiscount(disc) },
                            onSetApplyTax = { apply -> viewModel.setApplyTax(apply) },
                            onSetCartNotes = { notes -> viewModel.setCartNotes(notes) },
                            onHoldCart = { viewModel.holdCurrentCart() },
                            onRestoreCart = { parked -> viewModel.restoreParkedCart(parked) },
                            onCheckoutConfirm = { method, paid ->
                                viewModel.processCheckout(method, paid) { }
                            }
                        )
                    }
                    AppScreen.PRODUCTS -> {
                        ProductManagementScreen(
                            products = products,
                            categories = categories,
                            currentUser = currentUser,
                            onSaveProduct = { prod -> viewModel.saveProduct(prod) },
                            onDeleteProduct = { prod -> viewModel.deleteProduct(prod) }
                        )
                    }
                    AppScreen.CATEGORIES -> {
                        CategoryScreen(
                            categories = categories,
                            onAddCategory = { name -> viewModel.addCategory(name) },
                            onDeleteCategory = { cat -> viewModel.deleteCategory(cat) }
                        )
                    }
                    AppScreen.STOCK -> {
                        StockManagementScreen(
                            products = products,
                            stockMovements = stockMovements,
                            currentUser = currentUser,
                            onAdjustStock = { id, type, amount, reason ->
                                viewModel.adjustStock(id, type, amount, reason)
                            }
                        )
                    }
                    AppScreen.PRICE_MANAGEMENT -> {
                        PriceManagementScreen(
                            products = products,
                            priceHistoryList = priceHistoryList,
                            currentUser = currentUser,
                            onUpdatePrice = { id, newSelling, newCost, notes ->
                                viewModel.updateProductPrice(id, newSelling, newCost, notes)
                            }
                        )
                    }
                    AppScreen.TRANSACTIONS -> {
                        TransactionHistoryScreen(
                            transactions = transactions,
                            settings = settings,
                            currentUser = currentUser,
                            onCancelTransaction = { trans, reason ->
                                viewModel.cancelTransaction(trans, reason)
                            }
                        )
                    }
                    AppScreen.CUSTOMERS -> {
                        CustomerScreen(
                            customers = customers,
                            onSaveCustomer = { cust -> viewModel.saveCustomer(cust) },
                            onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) }
                        )
                    }
                    AppScreen.REPORTS -> {
                        ReportScreen(
                            transactions = transactions,
                            settings = settings
                        )
                    }
                    AppScreen.USERS -> {
                        UserManagementScreen(
                            users = users,
                            currentUser = currentUser,
                            onSaveUser = { user -> viewModel.saveUser(user) },
                            onDeleteUser = { user -> viewModel.deleteUser(user) }
                        )
                    }
                    AppScreen.BACKUP_RESTORE -> {
                        BackupRestoreScreen(
                            onExportJson = { viewModel.getBackupJson() },
                            onRestoreJson = { json, cb -> viewModel.restoreBackup(json, cb) },
                            onResetDemo = { viewModel.resetToDemo() }
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            currentSettings = settings,
                            isDarkMode = isDarkMode,
                            onSaveSettings = { newSettings -> viewModel.saveSettings(newSettings) }
                        )
                    }
                    AppScreen.ABOUT_DONATE -> {
                        AboutDonateScreen()
                    }
                }
            }
        }
    }

    // Thermal Receipt Dialog upon Checkout completion
    lastCompletedTransaction?.let { trans ->
        ThermalReceiptDialog(
            transaction = trans,
            settings = settings ?: ShopSettings(),
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // User Switch Dialog
    if (showUserSwitchDialog) {
        UserSwitchDialog(
            users = users,
            currentUser = currentUser,
            onDismiss = { showUserSwitchDialog = false },
            onConfirmSwitch = { user, pin ->
                viewModel.switchUser(user, pin) {
                    showUserSwitchDialog = false
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                        scope.launch { drawerState.close() }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showLogoutConfirmDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}
