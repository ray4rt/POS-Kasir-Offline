package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(
    onExportJson: suspend () -> String,
    onRestoreJson: (String, (Boolean) -> Unit) -> Unit,
    onResetDemo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var backupText by remember { mutableStateOf("") }
    var restoreInput by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Backup & Restore Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Amankan database kasir secara offline dalam format JSON",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Backup Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldPrimary)
                        Text("1. Ekspor Backup Database", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Simpan seluruh data produk, riwayat transaksi, pelanggan, dan pengaturan dalam format JSON.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                backupText = onExportJson()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate JSON Backup")
                    }

                    if (backupText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("POS Backup", backupText))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.padding(start = 6.dp))
                                Text("Salin", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "Backup Database Kasir Offline")
                                        putExtra(Intent.EXTRA_TEXT, backupText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Bagikan Backup JSON"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.padding(start = 6.dp))
                                Text("Bagikan", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Restore Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF3B82F6))
                        Text("2. Pulihkan (Restore) Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Tempelkan teks JSON backup untuk memulihkan data toko.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = restoreInput,
                        onValueChange = { restoreInput = it },
                        placeholder = { Text("Tempel JSON di sini...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (restoreInput.isNotBlank()) {
                                showRestoreConfirm = true
                            }
                        },
                        enabled = restoreInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Database")
                    }
                }
            }
        }

        // Reset Demo Data Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Reset Data Demo Awal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Muat ulang data demo (Beras, Gula, Minyak, Telur, dll). Transaksi dan data saat ini akan digantikan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Muat Ulang Data Demo")
                    }
                }
            }
        }
    }

    if (showRestoreConfirm) {
        ConfirmationDialog(
            title = "Pulihkan Database",
            message = "Perhatian: Database yang ada saat ini akan ditimpa dengan data dari JSON backup. Lanjutkan?",
            confirmText = "Ya, Pulihkan",
            isDestructive = true,
            onConfirm = {
                onRestoreJson(restoreInput) {
                    showRestoreConfirm = false
                    restoreInput = ""
                }
            },
            onDismiss = { showRestoreConfirm = false }
        )
    }

    if (showResetConfirm) {
        ConfirmationDialog(
            title = "Reset ke Data Demo",
            message = "Apakah Anda yakin ingin memuat kembali data demo awal retail?",
            confirmText = "Ya, Muat Demo",
            isDestructive = true,
            onConfirm = {
                onResetDemo()
                showResetConfirm = false
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}
