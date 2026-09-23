package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.EmeraldPrimary

@Composable
fun UserManagementScreen(
    users: List<UserAccount>,
    currentUser: UserAccount,
    onSaveUser: (UserAccount) -> Unit,
    onDeleteUser: (UserAccount) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserAccount?>(null) }
    var userToDelete by remember { mutableStateOf<UserAccount?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Manajemen Pengguna & PIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kelola akun kasir dan hak akses admin beserta PIN keamanan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(users) { user ->
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
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.padding(start = 8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (user.role == "ADMIN") EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = user.role,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.role == "ADMIN") EmeraldPrimary else Color(0xFF3B82F6),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Username: @${user.username} • PIN: ••••", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row {
                                IconButton(onClick = {
                                    userToEdit = user
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                if (users.size > 1 && user.id != currentUser.id) {
                                    IconButton(onClick = { userToDelete = user }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                userToEdit = null
                showDialog = true
            },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Pengguna")
        }

        if (showDialog) {
            var username by remember { mutableStateOf(userToEdit?.username ?: "") }
            var fullName by remember { mutableStateOf(userToEdit?.fullName ?: "") }
            var role by remember { mutableStateOf(userToEdit?.role ?: "KASIR") }
            var pinCode by remember { mutableStateOf(userToEdit?.pinCode ?: "") }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (userToEdit == null) "Tambah Pengguna" else "Edit Pengguna", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nama Lengkap") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        Text("Role Akses:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            listOf("ADMIN", "KASIR").forEach { r ->
                                FilterChip(
                                    selected = role == r,
                                    onClick = { role = r },
                                    label = { Text(r) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("PIN Keamanan (4-6 digit)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (username.isNotBlank() && pinCode.isNotBlank()) {
                                val saved = (userToEdit ?: UserAccount(username = username, fullName = fullName, role = role, pinCode = pinCode)).copy(
                                    username = username.trim(),
                                    fullName = fullName.trim(),
                                    role = role,
                                    pinCode = pinCode.trim()
                                )
                                onSaveUser(saved)
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        userToDelete?.let { user ->
            ConfirmationDialog(
                title = "Hapus Pengguna",
                message = "Hapus akun '${user.fullName}'?",
                isDestructive = true,
                onConfirm = {
                    onDeleteUser(user)
                    userToDelete = null
                },
                onDismiss = { userToDelete = null }
            )
        }
    }
}
