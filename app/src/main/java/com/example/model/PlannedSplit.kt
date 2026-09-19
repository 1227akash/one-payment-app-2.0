package com.example.model

data class PlannedSplit(
    val trancheIndex: Int,
    val amount: Double,
    val bankAccount: BankAccount?,
    val description: String,
    val isComplianceCompliant: Boolean = true,
    val disclaimer: String = "Splitting a payment does not guarantee that applicable charges will be avoided. Every transaction must follow official provider rules and requires explicit user authorization."
)

data class PaymentPlanResult(
    val totalAmount: Double,
    val splits: List<PlannedSplit>,
    val providerNote: String,
    val statutoryDisclaimer: String = "Splitting a payment does not guarantee that applicable charges will be avoided."
)
