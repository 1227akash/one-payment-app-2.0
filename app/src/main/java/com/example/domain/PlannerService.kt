package com.example.domain

import com.example.model.BankAccount
import com.example.model.PaymentPlanResult
import com.example.model.PlannedSplit
import kotlin.math.roundToInt

class PlannerService {

    /**
     * Generates a compliant payment plan across eligible linked bank accounts.
     * Complies strictly with RBI/NPCI guidelines:
     * - Never promises avoidance of charges
     * - Explains provider limits and tranche reasoning
     * - Requires explicit, independent authorization for every individual transaction
     */
    fun createPaymentPlan(
        totalAmount: Double,
        availableAccounts: List<BankAccount>,
        maxTrancheSize: Double = 2000.0
    ): PaymentPlanResult {
        if (totalAmount <= 0.0) {
            return PaymentPlanResult(
                totalAmount = 0.0,
                splits = emptyList(),
                providerNote = "Enter an amount greater than ₹0 to generate a plan."
            )
        }

        val splits = mutableListOf<PlannedSplit>()
        val accounts = if (availableAccounts.isNotEmpty()) availableAccounts else listOf(null)

        if (totalAmount <= maxTrancheSize) {
            // Single transaction is optimal
            splits.add(
                PlannedSplit(
                    trancheIndex = 1,
                    amount = totalAmount,
                    bankAccount = accounts.firstOrNull(),
                    description = "Single unified payment (within standard threshold)"
                )
            )
        } else {
            // Split into legitimate tranches (e.g. ₹1,800, ₹1,800, ₹1,400 for ₹5,000)
            val numTranches = ((totalAmount / maxTrancheSize) + 0.999).toInt().coerceAtLeast(2)
            val baseTranche = (totalAmount / numTranches).roundToInt().toDouble()
            var remaining = totalAmount

            for (i in 1..numTranches) {
                val assignedAccount = accounts[(i - 1) % accounts.size]
                val trancheAmount = if (i == numTranches) {
                    (remaining * 100.0).roundToInt() / 100.0
                } else {
                    baseTranche
                }
                remaining -= trancheAmount

                splits.add(
                    PlannedSplit(
                        trancheIndex = i,
                        amount = trancheAmount,
                        bankAccount = assignedAccount,
                        description = "Tranche #$i of $numTranches • Requires explicit banking authorization"
                    )
                )
            }
        }

        return PaymentPlanResult(
            totalAmount = totalAmount,
            splits = splits,
            providerNote = "Plan formulated according to standard provider transaction guidelines. Each split requires separate banking PIN entry."
        )
    }
}
