package com.example.model

data class MdrCalculation(
    val amount: Double,
    val category: MerchantCategory,
    val instrument: PaymentInstrument,
    val ratePercentage: Double,
    val baseCharge: Double,
    val gstRatePercentage: Double = 18.0,
    val gstAmount: Double,
    val totalFee: Double,
    val maxCap: Double?,
    val isCapped: Boolean,
    val customerPayableTotal: Double,
    val netMerchantSettlement: Double = (amount - totalFee).coerceAtLeast(0.0),
    val officialNotice: String = "MDR (Merchant Discount Rate) is not a customer tax; it is an interchange/processing fee set under regulatory guidelines."
)
