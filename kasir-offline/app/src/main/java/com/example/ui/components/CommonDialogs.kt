package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter
import com.example.util.ReceiptGenerator
import com.example.util.UnitConverter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeightQtyDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, unit: String) -> Unit
) {
    val convertibleUnits = remember(product.unit) {
        UnitConverter.getConvertibleUnits(product.unit)
    }
    var selectedUnit by remember { mutableStateOf(product.unit) }
    var qtyInput by remember { mutableStateOf("1") }

    val currentQty = qtyInput.replace(',', '.').toDoubleOrNull() ?: 0.0
    val calculatedSubtotal = remember(currentQty, selectedUnit) {
        UnitConverter.calculateItemTotal(
            basePrice = product.sellingPrice,
            inputQty = currentQty,
            inputUnit = selectedUnit,
            baseUnit = product.unit
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${CurrencyFormatter.format(product.sellingPrice)} / ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Unit Selection (e.g. kg vs gram vs ons)
                if (convertibleUnits.size > 1) {
                    Text(
                        text = "Pilih Satuan:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        convertibleUnits.forEach { unit ->
                            FilterChip(
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit },
                                label = { Text(unit) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Quick preset buttons for weights/quantities
                Text(
                    text = "Pilihan Cepat:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    val presets = if (selectedUnit == "kg" || selectedUnit == "liter" || selectedUnit == "meter") {
                        listOf("0.1", "0.25", "0.5", "0.75", "1", "1.5", "2")
                    } else if (selectedUnit == "gram" || selectedUnit == "ml") {
                        listOf("100", "250", "350", "500", "750", "1000")
                    } else if (selectedUnit == "ons") {
                        listOf("1", "2.5", "5", "7.5", "10")
                    } else {
                        listOf("1", "2", "3", "5", "10", "12")
                    }

                    presets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { qtyInput = preset }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$preset $selectedUnit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Input Field with + and -
                Text(
                    text = "Jumlah / Berat:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            val newV = (currentQty - 1.0).coerceAtLeast(0.1)
                            qtyInput = UnitConverter.formatQty(newV)
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang")
                    }

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text(selectedUnit, modifier = Modifier.padding(end = 8.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("qty_input_field"),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            val newV = currentQty + 1.0
                            qtyInput = UnitConverter.formatQty(newV)
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time calculation preview card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kalkulasi Real-time:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (selectedUnit != product.unit) {
                                val baseQty = UnitConverter.convertToBaseUnitQty(currentQty, selectedUnit, product.unit)
                                Text(
                                    text = "(= ${UnitConverter.formatQty(baseQty)} ${product.unit})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(calculatedSubtotal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentQty > 0.0) {
                        onConfirm(currentQty, selectedUnit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_qty_button")
            ) {
                Text("Masukkan Keranjang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentDialog(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (method: String, paidAmount: Double) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Cash") }
    var paidAmountInput by remember { mutableStateOf(CurrencyFormatter.formatNumber(totalAmount)) }

    val methods = listOf("Cash", "QRIS", "Transfer", "Debit", "Kredit", "E-wallet", "Lainnya")

    val currentPaid = CurrencyFormatter.parseAmount(paidAmountInput)
    val changeAmount = if (selectedMethod == "Cash") (currentPaid - totalAmount).coerceAtLeast(0.0) else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pembayaran Kasir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Total Amount Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text(
                            text = "TOTAL TAGIHAN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyFormatter.format(totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Payment Method Selector
                Text(
                    text = "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = {
                                selectedMethod = method
                                if (method != "Cash") {
                                    paidAmountInput = CurrencyFormatter.formatNumber(totalAmount)
                                }
                            },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                if (selectedMethod == "Cash") {
                    // Quick Cash Buttons
                    Text(
                        text = "Uang Pas / Nominal Cepat:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { paidAmountInput = CurrencyFormatter.formatNumber(totalAmount) }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Uang Pas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        val quickValues = listOf(10000.0, 20000.0, 50000.0, 100000.0, 200000.0)
                        quickValues.filter { it >= totalAmount || it == 50000.0 || it == 100000.0 }.forEach { value ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { paidAmountInput = CurrencyFormatter.formatNumber(value) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = CurrencyFormatter.format(value),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Paid Input Field
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = { paidAmountInput = it },
                        label = { Text("Uang Diterima") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paid_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Change Calculation
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentPaid >= totalAmount) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentPaid >= totalAmount) "Kembalian:" else "Kurang Bayar:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (currentPaid >= totalAmount) CurrencyFormatter.format(changeAmount) else CurrencyFormatter.format(totalAmount - currentPaid),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentPaid >= totalAmount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Pembayaran via $selectedMethod tercatat langsung lunas secara offline.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPaid = if (selectedMethod == "Cash") currentPaid else totalAmount
                    onConfirmPayment(selectedMethod, finalPaid)
                },
                enabled = selectedMethod != "Cash" || currentPaid >= totalAmount,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Selesaikan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ThermalReceiptDialog(
    transaction: TransactionEntity,
    settings: ShopSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptText = remember(transaction, settings) {
        ReceiptGenerator.generateTextReceipt(transaction, settings)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Struk Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Receipt Simulation View (Monospace styled thermal paper)
                Surface(
                    color = Color(0xFFFAF8F5), // subtle off-white thermal paper
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 360.dp)
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = receiptText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF1E1E1E),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Copy, Share, Print
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Struk ${transaction.invoiceNumber}", receiptText)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Struk ${transaction.invoiceNumber}")
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Struk"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagi", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            // Thermal printer print action via share or system print
                            val printIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                            }
                            context.startActivity(Intent.createChooser(printIntent, "Kirim ke Thermal Printer"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cetak", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UserSwitchDialog(
    users: List<UserAccount>,
    currentUser: UserAccount,
    onDismiss: () -> Unit,
    onConfirmSwitch: (UserAccount, String) -> Unit
) {
    var selectedUser by remember { mutableStateOf(currentUser) }
    var pinInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ganti Pengguna Kasir / Admin", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Pilih Akun:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                users.forEach { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedUser = user }
                            .background(
                                if (selectedUser.id == user.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (selectedUser.id == user.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(user.fullName, fontWeight = FontWeight.SemiBold)
                            Text("Role: ${user.role} (@${user.username})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) pinInput = it },
                    label = { Text("Masukkan PIN (${selectedUser.role})") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "Default PIN: Admin = 1234, Kasir = 0000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSwitch(selectedUser, pinInput) },
                enabled = pinInput.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Ya, Lanjutkan",
    dismissText: String = "Batal",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
