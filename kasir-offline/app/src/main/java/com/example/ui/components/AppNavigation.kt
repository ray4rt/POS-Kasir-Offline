package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopSettings
import com.example.data.model.UserAccount
import com.example.ui.AppScreen
import com.example.ui.theme.EmeraldPrimary

fun getScreenIcon(screen: AppScreen): ImageVector {
    return when (screen) {
        AppScreen.DASHBOARD -> Icons.Default.Dashboard
        AppScreen.CASHIER -> Icons.Default.PointOfSale
        AppScreen.PRODUCTS -> Icons.Default.Inventory2
        AppScreen.CATEGORIES -> Icons.Default.Category
        AppScreen.STOCK -> Icons.Default.Warehouse
        AppScreen.PRICE_MANAGEMENT -> Icons.Default.PriceChange
        AppScreen.TRANSACTIONS -> Icons.AutoMirrored.Filled.ReceiptLong
        AppScreen.CUSTOMERS -> Icons.Default.People
        AppScreen.REPORTS -> Icons.Default.Analytics
        AppScreen.USERS -> Icons.Default.ManageAccounts
        AppScreen.BACKUP_RESTORE -> Icons.Default.Backup
        AppScreen.SETTINGS -> Icons.Default.Settings
        AppScreen.ABOUT_DONATE -> Icons.Default.VolunteerActivism
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopAppBar(
    settings: ShopSettings?,
    currentUser: UserAccount,
    isDarkMode: Boolean,
    cartItemCount: Int,
    lowStockCount: Int,
    onMenuClick: () -> Unit,
    onUserSwitchClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = settings?.shopName ?: "Kasir Offline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Mode Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("nav_menu_button")
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu Navigasi")
            }
        },
        actions = {
            // Dark Mode Toggle
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Ganti Tema"
                )
            }

            // Logout Button
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Keluar",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            // User Role Chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable(onClick = onUserSwitchClick)
                    .padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (currentUser.role == "ADMIN") EmeraldPrimary else Color(0xFF3B82F6),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentUser.fullName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun DrawerContent(
    currentScreen: AppScreen,
    currentUser: UserAccount,
    lowStockCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header in Drawer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(EmeraldPrimary, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "POS KASIR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Sistem Kasir Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Menu items
        AppScreen.values().forEach { screen ->
            val isSelected = currentScreen == screen
            val isRestricted = screen.adminOnly && currentUser.role != "ADMIN"

            NavigationDrawerItem(
                label = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = screen.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isRestricted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                        )
                        if (screen == AppScreen.STOCK && lowStockCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$lowStockCount", color = Color.White)
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = getScreenIcon(screen),
                        contentDescription = screen.label,
                        tint = if (isSelected) EmeraldPrimary else if (isRestricted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = isSelected,
                onClick = {
                    onNavigate(screen)
                    onCloseDrawer()
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    selectedIconColor = EmeraldPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_item_${screen.name.lowercase()}")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        NavigationDrawerItem(
            label = { Text("Keluar (Logout)", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            selected = false,
            onClick = onLogout,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.error,
                unselectedTextColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "v1.0.0 • Dev by Rayden",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun PosBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenMore: () -> Unit
) {
    val quickScreens = listOf(
        AppScreen.CASHIER,
        AppScreen.DASHBOARD,
        AppScreen.TRANSACTIONS,
        AppScreen.PRODUCTS
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        quickScreens.forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(getScreenIcon(screen), contentDescription = screen.label)
                },
                label = { Text(screen.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    indicatorColor = EmeraldPrimary
                )
            )
        }

        NavigationBarItem(
            selected = currentScreen !in quickScreens,
            onClick = onOpenMore,
            icon = {
                Icon(Icons.Default.Menu, contentDescription = "Lainnya")
            },
            label = { Text("Menu", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
