package com.example.domain

import com.example.model.MdrCalculation
import com.example.model.MerchantCategory
import com.example.model.PaymentInstrument
import kotlin.math.min
import kotlin.math.roundToInt

class ChargeCalculatorService {

    /**
     * Calculates regulatory MDR and customer processing fees.
     * Compliant with NPCI / RBI UPI guidelines.
     */
    fun calculate(
        amount: Double,
        category: MerchantCategory,
        instrument: PaymentInstrument
    ): MdrCalculation {
        if (amount <= 0.0) {
            return MdrCalculation(
                amount = 0.0,
                category = category,
                instrument = instrument,
                ratePercentage = 0.0,
                baseCharge = 0.0,
                gstAmount = 0.0,
                totalFee = 0.0,
                maxCap = null,
                isCapped = false,
                customerPayableTotal = 0.0
            )
        }

        // Standard UPI Bank Account to Bank Account is 0% MDR by government mandate
        var rate = category.baseRate
        var maxCap = category.cap

        if (instrument == PaymentInstrument.BANK_UPI) {
            // Direct bank-to-bank UPI has 0% customer & merchant MDR under current guidelines
            rate = 0.0
            maxCap = 0.0
        } else if (instrument == PaymentInstrument.RUPAY_CREDIT) {
            // RuPay Credit Card on UPI has interchange for transactions > ₹2000
            rate = if (amount <= 2000.0) 0.0 else 0.014 // 1.4%
            maxCap = 150.0
        } else if (instrument == PaymentInstrument.PPI_WALLET) {
            // Prepaid Payment Instruments (Wallets) interchange on merchant UPI > ₹2000
            rate = if (amount <= 2000.0) 0.0 else 0.011 // 1.1%
            maxCap = 250.0
        }

        val rawBase = amount * rate
        val isCapped = maxCap != null && rawBase > maxCap
        val baseCharge = if (maxCap != null) min(rawBase, maxCap) else rawBase

        // GST is 18% applied specifically on the processing fee, not the principal amount
        val gstAmount = baseCharge * 0.18
        val totalFee = (baseCharge + gstAmount).roundTwoDecimals()
        val customerPayable = (amount + totalFee).roundTwoDecimals()

        return MdrCalculation(
            amount = amount,
            category = category,
            instrument = instrument,
            ratePercentage = (rate * 100).roundTwoDecimals(),
            baseCharge = baseCharge.roundTwoDecimals(),
            gstAmount = gstAmount.roundTwoDecimals(),
            totalFee = totalFee,
            maxCap = maxCap,
            isCapped = isCapped,
            customerPayableTotal = customerPayable
        )
    }

    private fun Double.roundTwoDecimals(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }
}
