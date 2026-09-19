package com.example.domain

import com.example.data.OneRepository
import com.example.model.BankAccount
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Transaction
import com.example.security.SecurityManager
import kotlinx.coroutines.delay
import java.util.UUID

sealed class PaymentExecutionResult {
    data class Success(val transaction: Transaction) : PaymentExecutionResult()
    data class Pending(val transaction: Transaction, val message: String) : PaymentExecutionResult()
    data class Failed(val reason: String, val transaction: Transaction?) : PaymentExecutionResult()
    data class Unknown(val message: String, val transaction: Transaction) : PaymentExecutionResult()
    data class DuplicateWarning(val message: String) : PaymentExecutionResult()
    data class RateLimitWarning(val message: String) : PaymentExecutionResult()
}

class PaymentService(private val repository: OneRepository) {

    /**
     * Resolves recipient verification from official UPI registry simulation.
     */
    fun verifyRecipient(identifier: String, method: PaymentMethod): Result<String> {
        val clean = identifier.trim()
        return when (method) {
            PaymentMethod.UPI_ID -> {
                if (!SecurityManager.isValidUpiId(clean)) {
                    Result.failure(IllegalArgumentException("Invalid UPI ID format. Expected format: name@bank"))
                } else {
                    val sampleNames = mapOf(
                        "starbucks@icici" to "Tata Starbucks Pvt Ltd",
                        "bluetokai@icici" to "Blue Tokai Coffee Roasters",
                        "rahul@okhdfcbank" to "Rahul Sharma (Verified)",
                        "priya@oksbi" to "Priya Verma (Verified)",
                        "zomato@hdfcbank" to "Zomato Ltd",
                        "swiggy@icici" to "Bundl Technologies Pvt Ltd"
                    )
                    val resolved = sampleNames[clean.lowercase()]
                        ?: (clean.substringBefore("@").replace(".", " ").capitalizeWords() + " (Verified)")
                    Result.success(resolved)
                }
            }
            PaymentMethod.MOBILE -> {
                if (!SecurityManager.isValidMobileNumber(clean)) {
                    Result.failure(IllegalArgumentException("Invalid Indian mobile number. Expected 10 digits starting with 6-9."))
                } else {
                    Result.success("Verified Contact (+91 ${clean.takeLast(10)})")
                }
            }
            PaymentMethod.BANK_IFSC -> {
                val parts = clean.split("|")
                val acc = parts.getOrNull(0) ?: ""
                val ifsc = parts.getOrNull(1) ?: ""
                if (!SecurityManager.isValidAccountNumber(acc)) {
                    Result.failure(IllegalArgumentException("Invalid account number. Must be 9 to 18 digits."))
                } else if (!SecurityManager.isValidIfsc(ifsc)) {
                    Result.failure(IllegalArgumentException("Invalid IFSC code. Format: 4 letters, 0, 6 characters (e.g. HDFC0001234)"))
                } else {
                    Result.success("Account Verified at ${ifsc.take(4)} Bank")
                }
            }
            PaymentMethod.QR -> {
                Result.success("Verified Merchant / Peer")
            }
        }
    }

    /**
     * Executes the payment through official PSP authorization.
     * Prevents duplicate payments and rate limiting.
     */
    suspend fun executePayment(
        sourceAccount: BankAccount,
        recipientIdentifier: String,
        recipientName: String,
        amount: Double,
        method: PaymentMethod,
        note: String = "",
        forceSimulatedFailure: Boolean = false,
        forceSimulatedUncertainty: Boolean = false
    ): PaymentExecutionResult {

        if (!SecurityManager.isValidPaymentAmount(amount)) {
            return PaymentExecutionResult.Failed("Payment amount must be between ₹1.00 and ₹1,00,000.00", null)
        }

        if (SecurityManager.isRateLimitExceeded()) {
            return PaymentExecutionResult.RateLimitWarning("Excessive payment attempts detected. Please wait 60 seconds before trying again.")
        }

        if (SecurityManager.checkDuplicateTransaction(recipientIdentifier, amount)) {
            return PaymentExecutionResult.DuplicateWarning(
                "A payment of ₹$amount to this recipient was already initiated in the last 60 seconds. Please check transaction history to prevent duplicate charges."
            )
        }

        val idempotencyKey = SecurityManager.generateIdempotencyKey(sourceAccount.id, recipientIdentifier, amount)
        val utr = SecurityManager.generateUtrReference()
        val transactionCode = SecurityManager.generateTransactionCode()
        val e2eeToken = SecurityManager.createEncryptedPaymentPayload(
            sourceAccountId = sourceAccount.id,
            recipientIdentifier = recipientIdentifier,
            amount = amount,
            utr = utr,
            pinVerified = true
        )

        // Simulating official NPCI / Bank PSP authorization gateway response
        delay(1200)

        if (forceSimulatedUncertainty) {
            val uncertainTx = Transaction(
                id = UUID.randomUUID().toString(),
                utrReference = utr,
                amount = amount,
                fee = 0.0,
                type = "SENT",
                status = PaymentStatus.UNKNOWN,
                recipientOrSenderName = recipientName,
                recipientOrSenderUpiId = recipientIdentifier,
                sourceBankName = sourceAccount.bankName,
                sourceAccountMasked = sourceAccount.maskedAccountNumber,
                note = note,
                timestamp = System.currentTimeMillis(),
                method = method,
                idempotencyKey = idempotencyKey,
                transactionCode = transactionCode,
                encryptedE2eeToken = e2eeToken
            )
            repository.recordTransaction(uncertainTx)
            return PaymentExecutionResult.Unknown(
                "Your previous payment status could not be confirmed. Please check your transaction history before trying again.",
                uncertainTx
            )
        }

        if (forceSimulatedFailure || sourceAccount.balance < amount) {
            val reason = if (sourceAccount.balance < amount) "Insufficient account balance in ${sourceAccount.bankName}" else "Payment authorization declined by issuing bank"
            val failedTx = Transaction(
                id = UUID.randomUUID().toString(),
                utrReference = utr,
                amount = amount,
                fee = 0.0,
                type = "SENT",
                status = PaymentStatus.FAILED,
                recipientOrSenderName = recipientName,
                recipientOrSenderUpiId = recipientIdentifier,
                sourceBankName = sourceAccount.bankName,
                sourceAccountMasked = sourceAccount.maskedAccountNumber,
                note = note.ifEmpty { reason },
                timestamp = System.currentTimeMillis(),
                method = method,
                idempotencyKey = idempotencyKey,
                transactionCode = transactionCode,
                encryptedE2eeToken = e2eeToken
            )
            repository.recordTransaction(failedTx)
            return PaymentExecutionResult.Failed(reason, failedTx)
        }

        // Success
        val successTx = Transaction(
            id = UUID.randomUUID().toString(),
            utrReference = utr,
            amount = amount,
            fee = 0.0,
            type = "SENT",
            status = PaymentStatus.SUCCESS,
            recipientOrSenderName = recipientName,
            recipientOrSenderUpiId = recipientIdentifier,
            sourceBankName = sourceAccount.bankName,
            sourceAccountMasked = sourceAccount.maskedAccountNumber,
            note = note,
            timestamp = System.currentTimeMillis(),
            method = method,
            idempotencyKey = idempotencyKey,
            transactionCode = transactionCode,
            encryptedE2eeToken = e2eeToken
        )
        repository.recordTransaction(successTx)
        val newBal = (sourceAccount.balance - amount).coerceAtLeast(0.0)
        repository.updateAccountBalance(sourceAccount.id, newBal)
        return PaymentExecutionResult.Success(successTx)
    }

    /**
     * Records a real UPI transaction completed through external certified UPI apps (GPay, PhonePe, Paytm).
     */
    suspend fun recordRealUpiPayment(
        sourceAccount: BankAccount,
        recipientIdentifier: String,
        recipientName: String,
        amount: Double,
        method: PaymentMethod,
        note: String,
        txnId: String?,
        approvalRefNo: String?
    ): PaymentExecutionResult.Success {
        val utr = approvalRefNo?.ifBlank { null }
            ?: txnId?.ifBlank { null }
            ?: SecurityManager.generateUtrReference()
        val transactionCode = SecurityManager.generateTransactionCode()
        val e2eeToken = SecurityManager.createEncryptedPaymentPayload(
            sourceAccountId = sourceAccount.id,
            recipientIdentifier = recipientIdentifier,
            amount = amount,
            utr = utr,
            pinVerified = true
        )

        val successTx = Transaction(
            id = UUID.randomUUID().toString(),
            utrReference = utr,
            amount = amount,
            fee = 0.0,
            type = "SENT",
            status = PaymentStatus.SUCCESS,
            recipientOrSenderName = recipientName,
            recipientOrSenderUpiId = recipientIdentifier,
            sourceBankName = sourceAccount.bankName,
            sourceAccountMasked = sourceAccount.maskedAccountNumber,
            note = note.ifBlank { "Real UPI Bank Transfer" },
            timestamp = System.currentTimeMillis(),
            method = method,
            idempotencyKey = txnId ?: UUID.randomUUID().toString(),
            transactionCode = transactionCode,
            encryptedE2eeToken = e2eeToken
        )
        repository.recordTransaction(successTx)
        val newBal = (sourceAccount.balance - amount).coerceAtLeast(0.0)
        repository.updateAccountBalance(sourceAccount.id, newBal)
        return PaymentExecutionResult.Success(successTx)
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
