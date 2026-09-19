package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey val id: String,
    val bankName: String,
    val bankCode: String, // e.g. HDFC, SBI, ICICI, AXIS
    val maskedAccountNumber: String, // e.g. "XXXX XXXX 4582"
    val upiId: String,
    val ifscPrefix: String,
    val accountType: String = "Savings",
    val isDefault: Boolean = false,
    val balance: Double = 25000.0,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val linkedAt: Long = System.currentTimeMillis()
)
