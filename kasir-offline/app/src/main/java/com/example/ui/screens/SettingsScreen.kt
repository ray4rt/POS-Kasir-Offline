package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ShopSettings
import com.example.ui.theme.EmeraldPrimary

@Composable
fun SettingsScreen(
    currentSettings: ShopSettings?,
    isDarkMode: Boolean,
    onSaveSettings: (ShopSettings) -> Unit
) {
    val settings = currentSettings ?: ShopSettings()

    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var receiptFooter by remember(settings) { mutableStateOf(settings.receiptFooter) }
    var currency by remember(settings) { mutableStateOf(settings.currency) }
    var taxPercentage by remember(settings) { mutableStateOf(settings.taxPercentage.toInt().toString()) }
    var printerWidth by remember(settings) { mutableStateOf(settings.printerWidth) }
    var darkModeVal by remember(settings, isDarkMode) { mutableStateOf(isDarkMode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Pengaturan Toko & Printer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Atur profil bisnis, konfigurasi struk, dan preferensi tampilan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profil Toko", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Nama Toko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Toko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Nomor Telepon / WA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Receipt & Printer Settings
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Konfigurasi Struk & Kasir", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Lebar Kertas Printer Thermal:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        listOf("58mm", "80mm").forEach { width ->
                            FilterChip(
                                selected = printerWidth == width,
                                onClick = { printerWidth = width },
                                label = { Text(width) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = taxPercentage,
                        onValueChange = { taxPercentage = it },
                        label = { Text("Persentase Pajak Default (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = receiptFooter,
                        onValueChange = { receiptFooter = it },
                        label = { Text("Catatan Kaki Struk (Footer)") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Appearance
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column {
                        Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.Bold)
                        Text("Tema hijau emerald modern ChatGPT", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkModeVal,
                        onCheckedChange = { darkModeVal = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        shopName = shopName.trim(),
                        address = address.trim(),
                        phone = phone.trim(),
                        receiptFooter = receiptFooter.trim(),
                        currency = currency.trim(),
                        taxPercentage = taxPercentage.toDoubleOrNull() ?: 0.0,
                        printerWidth = printerWidth,
                        isDarkMode = darkModeVal
                    )
                    onSaveSettings(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
