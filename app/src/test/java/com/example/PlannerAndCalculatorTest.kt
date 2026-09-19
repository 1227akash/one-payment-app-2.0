package com.example

import com.example.domain.ChargeCalculatorService
import com.example.domain.PlannerService
import com.example.model.BankAccount
import com.example.model.MerchantCategory
import com.example.model.PaymentInstrument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerAndCalculatorTest {

    private val plannerService = PlannerService()
    private val calculatorService = ChargeCalculatorService()

    @Test
    fun testPlannerSplits() {
        val accounts = listOf(
            BankAccount("1", "HDFC Bank", "HDFC", "XXXX 4582", "a@hdfc", "HDFC0001", balance = 20000.0),
            BankAccount("2", "SBI", "SBI", "XXXX 9217", "a@sbi", "SBIN0002", balance = 15000.0)
        )

        val result5000 = plannerService.createPaymentPlan(5000.0, accounts, maxTrancheSize = 2000.0)
        assertEquals(5000.0, result5000.totalAmount, 0.001)
        assertTrue(result5000.splits.size >= 2)
        val sum = result5000.splits.sumOf { it.amount }
        assertEquals(5000.0, sum, 0.05)

        // Verify mandatory statutory disclaimer
        assertTrue(result5000.statutoryDisclaimer.contains("Splitting a payment does not guarantee that applicable charges will be avoided"))

        val result1500 = plannerService.createPaymentPlan(1500.0, accounts, maxTrancheSize = 2000.0)
        assertEquals(1, result1500.splits.size)
        assertEquals(1500.0, result1500.splits[0].amount, 0.001)
    }

    @Test
    fun testBankUpiZeroMdr() {
        val result = calculatorService.calculate(
            amount = 5000.0,
            category = MerchantCategory.LARGE_MERCHANT,
            instrument = PaymentInstrument.BANK_UPI
        )
        assertEquals(0.0, result.ratePercentage, 0.001)
        assertEquals(0.0, result.baseCharge, 0.001)
        assertEquals(0.0, result.gstAmount, 0.001)
        assertEquals(0.0, result.totalFee, 0.001)
        assertEquals(5000.0, result.customerPayableTotal, 0.001)
        assertFalse(result.officialNotice.contains("tax unless defined"))
    }

    @Test
    fun testRupayCreditCardMdrAndGst() {
        val result = calculatorService.calculate(
            amount = 3000.0,
            category = MerchantCategory.LARGE_MERCHANT,
            instrument = PaymentInstrument.RUPAY_CREDIT
        )
        assertEquals(1.4, result.ratePercentage, 0.001)
        assertEquals(42.0, result.baseCharge, 0.001) // 3000 * 0.014 = 42
        assertEquals(7.56, result.gstAmount, 0.001) // 42 * 0.18 = 7.56
        assertEquals(49.56, result.totalFee, 0.001)
        assertEquals(3049.56, result.customerPayableTotal, 0.001)
    }

    @Test
    fun testPpiWalletInterchangeCapping() {
        // High amount to trigger cap
        val result = calculatorService.calculate(
            amount = 50000.0,
            category = MerchantCategory.WALLET_PPI,
            instrument = PaymentInstrument.PPI_WALLET
        )
        // Cap is 250
        assertEquals(250.0, result.baseCharge, 0.001)
        assertTrue(result.isCapped)
        assertEquals(45.0, result.gstAmount, 0.001) // 250 * 0.18 = 45
        assertEquals(295.0, result.totalFee, 0.001)
        assertEquals(50295.0, result.customerPayableTotal, 0.001)
    }
}
