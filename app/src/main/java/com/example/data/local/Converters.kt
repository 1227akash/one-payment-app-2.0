package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.AccountStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (_: Exception) {
        PaymentMethod.UPI_ID
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = try {
        PaymentStatus.valueOf(value)
    } catch (_: Exception) {
        PaymentStatus.SUCCESS
    }

    @TypeConverter
    fun fromAccountStatus(value: AccountStatus): String = value.name

    @TypeConverter
    fun toAccountStatus(value: String): AccountStatus = try {
        AccountStatus.valueOf(value)
    } catch (_: Exception) {
        AccountStatus.ACTIVE
    }
}
