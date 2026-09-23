package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String,          // "ADMIN" or "KASIR"
    val pinCode: String,       // 4 or 6 digit PIN
    val isActive: Boolean = true
)
