package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val uid: String,
    val displayName: String,
    val mobileNumber: String,
    val email: String,
    val upiHandle: String,
    val isKycVerified: Boolean = true,
    val biometricEnabled: Boolean = true,
    val preferredLanguage: String = "en", // "en" or "hi"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val paymentPinHash: String = "", // Salted SHA-256 hash of 6-digit payment security code
    val isFreshInstall: Boolean = true, // Indicates freshly installed state
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val sessionId: String,
    val deviceName: String,
    val clientPlatform: String = "Android",
    val ipAddressMasked: String,
    val lastActiveTimestamp: Long,
    val isCurrentDevice: Boolean
)
