package com.example

import com.example.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityManagerTest {

    @Test
    fun testUpiIdValidation() {
        assertTrue(SecurityManager.isValidUpiId("akash@okhdfcbank"))
        assertTrue(SecurityManager.isValidUpiId("merchant.store@icici"))
        assertTrue(SecurityManager.isValidUpiId("user123@paytm"))
        assertFalse(SecurityManager.isValidUpiId("invalid-upi-id"))
        assertFalse(SecurityManager.isValidUpiId("@okhdfcbank"))
        assertFalse(SecurityManager.isValidUpiId("akash@"))
    }

    @Test
    fun testIndianMobileValidation() {
        assertTrue(SecurityManager.isValidMobileNumber("9876543210"))
        assertTrue(SecurityManager.isValidMobileNumber("+919876543210"))
        assertTrue(SecurityManager.isValidMobileNumber("8123456789"))
        assertTrue(SecurityManager.isValidMobileNumber("7012345678"))
        assertTrue(SecurityManager.isValidMobileNumber("6234567890"))
        assertFalse(SecurityManager.isValidMobileNumber("1234567890")) // Doesn't start with 6-9
        assertFalse(SecurityManager.isValidMobileNumber("98765"))      // Too short
        assertFalse(SecurityManager.isValidMobileNumber("9876543210123")) // Too long
    }

    @Test
    fun testIfscValidation() {
        assertTrue(SecurityManager.isValidIfsc("HDFC0001234"))
        assertTrue(SecurityManager.isValidIfsc("SBIN0004567"))
        assertTrue(SecurityManager.isValidIfsc("ICIC0009876"))
        assertFalse(SecurityManager.isValidIfsc("HDFC1001234")) // 5th char must be 0
        assertFalse(SecurityManager.isValidIfsc("HDF0001234"))  // Too short
        assertFalse(SecurityManager.isValidIfsc("HDFC00012345")) // Too long
    }

    @Test
    fun testAccountNumberMasking() {
        val masked = SecurityManager.maskAccountNumber("123456784582")
        assertEquals("XXXX XXXX 4582", masked)

        val maskedShort = SecurityManager.maskAccountNumber("9217")
        assertEquals("XXXX XXXX 9217", maskedShort)
    }

    @Test
    fun testUpiIdMasking() {
        val masked = SecurityManager.maskUpiId("akash@okhdfcbank")
        assertEquals("ak***@okhdfcbank", masked)
    }

    @Test
    fun testIdempotencyKeyGeneration() {
        val key1 = SecurityManager.generateIdempotencyKey("acc1", "user@upi", 500.0)
        val key2 = SecurityManager.generateIdempotencyKey("acc1", "user@upi", 500.0)
        assertNotEquals(key1, key2)
        assertEquals(64, key1.length) // SHA-256 hex string length
    }

    @Test
    fun testDuplicateDetection() {
        val recipient = "test.unique.recipient@upi"
        val amount = 150.0
        val isDuplicateFirst = SecurityManager.checkDuplicateTransaction(recipient, amount)
        assertFalse(isDuplicateFirst)

        val isDuplicateSecond = SecurityManager.checkDuplicateTransaction(recipient, amount)
        assertTrue(isDuplicateSecond)
    }

    @Test
    fun testPaymentAmountLimits() {
        assertTrue(SecurityManager.isValidPaymentAmount(1.0))
        assertTrue(SecurityManager.isValidPaymentAmount(50000.0))
        assertTrue(SecurityManager.isValidPaymentAmount(100000.0))
        assertFalse(SecurityManager.isValidPaymentAmount(0.0))
        assertFalse(SecurityManager.isValidPaymentAmount(-50.0))
        assertFalse(SecurityManager.isValidPaymentAmount(100000.01))
    }

    @Test
    fun testUpiUriParsing() {
        val uri = "upi://pay?pa=starbucks@icici&pn=Tata%20Starbucks&am=250&cu=INR"
        val parsed = SecurityManager.parseUpiUri(uri)
        assertEquals("starbucks@icici", parsed["pa"])
        assertEquals("Tata Starbucks", parsed["pn"])
        assertEquals("250", parsed["am"])
        assertEquals("INR", parsed["cu"])
    }
}
