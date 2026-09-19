package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val utrReference: String,
    val amount: Double,
    val fee: Double = 0.0,
    val type: String, // "SENT" or "RECEIVED"
    val status: PaymentStatus,
    val recipientOrSenderName: String,
    val recipientOrSenderUpiId: String,
    val sourceBankName: String,
    val sourceAccountMasked: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val method: PaymentMethod,
    val idempotencyKey: String = "",
    val transactionCode: String = "",
    val encryptedE2eeToken: String = ""
)
