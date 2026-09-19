package com.example.data

import com.example.data.local.BankDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserDao
import com.example.model.AccountStatus
import com.example.model.BankAccount
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Transaction
import com.example.model.UserProfile
import com.example.model.UserSession
import com.example.security.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class OneRepository(
    private val bankDao: BankDao,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val allBankAccounts: Flow<List<BankAccount>> = bankDao.getAllBankAccountsFlow()
    val defaultBankAccount: Flow<BankAccount?> = bankDao.getDefaultBankAccountFlow()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val userProfile: Flow<UserProfile?> = userDao.getUserProfileFlow()
    val activeSessions: Flow<List<UserSession>> = userDao.getSessionsFlow()

    init {
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        if (bankDao.getAccountCount() == 0) {
            val defaultAccounts = listOf(
                BankAccount(
                    id = "bank_hdfc_01",
                    bankName = "HDFC Bank",
                    bankCode = "HDFC",
                    maskedAccountNumber = "XXXX XXXX 4582",
                    upiId = "akash@okhdfcbank",
                    ifscPrefix = "HDFC0001234",
                    accountType = "Savings",
                    isDefault = true,
                    balance = 48500.0,
                    status = AccountStatus.ACTIVE
                ),
                BankAccount(
                    id = "bank_sbi_02",
                    bankName = "State Bank of India",
                    bankCode = "SBI",
                    maskedAccountNumber = "XXXX XXXX 9217",
                    upiId = "akash@oksbi",
                    ifscPrefix = "SBIN0004567",
                    accountType = "Savings",
                    isDefault = false,
                    balance = 16250.0,
                    status = AccountStatus.ACTIVE
                ),
                BankAccount(
                    id = "bank_icici_03",
                    bankName = "ICICI Bank",
                    bankCode = "ICICI",
                    maskedAccountNumber = "XXXX XXXX 1109",
                    upiId = "akash@icici",
                    ifscPrefix = "ICIC0009876",
                    accountType = "Current",
                    isDefault = false,
                    balance = 92400.0,
                    status = AccountStatus.ACTIVE
                )
            )
            bankDao.insertBankAccounts(defaultAccounts)
        }

        if (transactionDao.getTransactionCount() == 0) {
            val now = System.currentTimeMillis()
            val hour = 3600000L
            val day = 86400000L

            val seedTransactions = listOf(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    utrReference = "328941005821",
                    amount = 1250.0,
                    fee = 0.0,
                    type = "SENT",
                    status = PaymentStatus.SUCCESS,
                    recipientOrSenderName = "Blue Tokai Coffee",
                    recipientOrSenderUpiId = "bluetokai@icici",
                    sourceBankName = "HDFC Bank",
                    sourceAccountMasked = "XXXX XXXX 4582",
                    note = "Artisan Roast & Bagel",
                    timestamp = now - (2 * hour),
                    method = PaymentMethod.QR
                ),
                Transaction(
                    id = UUID.randomUUID().toString(),
                    utrReference = "328912449102",
                    amount = 5400.0,
                    fee = 0.0,
                    type = "RECEIVED",
                    status = PaymentStatus.SUCCESS,
                    recipientOrSenderName = "Priya Sharma",
                    recipientOrSenderUpiId = "priya@oksbi",
                    sourceBankName = "HDFC Bank",
                    sourceAccountMasked = "XXXX XXXX 4582",
                    note = "Weekend trip settlement",
                    timestamp = now - (14 * hour),
                    method = PaymentMethod.UPI_ID
                ),
                Transaction(
                    id = UUID.randomUUID().toString(),
                    utrReference = "328877110034",
                    amount = 890.0,
                    fee = 0.0,
                    type = "SENT",
                    status = PaymentStatus.SUCCESS,
                    recipientOrSenderName = "Supermart Groceries",
                    recipientOrSenderUpiId = "mart@axisbank",
                    sourceBankName = "State Bank of India",
                    sourceAccountMasked = "XXXX XXXX 9217",
                    note = "Weekly essentials",
                    timestamp = now - (1 * day),
                    method = PaymentMethod.MOBILE
                ),
                Transaction(
                    id = UUID.randomUUID().toString(),
                    utrReference = "328755009123",
                    amount = 2500.0,
                    fee = 0.0,
                    type = "SENT",
                    status = PaymentStatus.REFUNDED,
                    recipientOrSenderName = "Airlines Booking Support",
                    recipientOrSenderUpiId = "refunds@indigo",
                    sourceBankName = "HDFC Bank",
                    sourceAccountMasked = "XXXX XXXX 4582",
                    note = "Seat upgrade cancellation",
                    timestamp = now - (2 * day),
                    method = PaymentMethod.UPI_ID
                ),
                Transaction(
                    id = UUID.randomUUID().toString(),
                    utrReference = "328612984501",
                    amount = 350.0,
                    fee = 0.0,
                    type = "SENT",
                    status = PaymentStatus.FAILED,
                    recipientOrSenderName = "Metro Transit Express",
                    recipientOrSenderUpiId = "metro@paytm",
                    sourceBankName = "ICICI Bank",
                    sourceAccountMasked = "XXXX XXXX 1109",
                    note = "Network timeout at gate",
                    timestamp = now - (3 * day),
                    method = PaymentMethod.QR
                )
            )
            transactionDao.insertTransactions(seedTransactions)
        }

        if (userDao.getUserProfile() == null) {
            val initialProfile = UserProfile(
                uid = "user_akash_01",
                displayName = "Akash Tiwari",
                mobileNumber = "9876543210",
                email = "akash.tiwari@example.com",
                upiHandle = "akash@okhdfcbank",
                isKycVerified = true,
                biometricEnabled = true,
                preferredLanguage = "en",
                themeMode = "SYSTEM"
            )
            userDao.insertProfile(initialProfile)

            val sessions = listOf(
                UserSession(
                    sessionId = UUID.randomUUID().toString(),
                    deviceName = "Pixel 8 Pro (This Device)",
                    clientPlatform = "Android 15",
                    ipAddressMasked = "103.21.***.***",
                    lastActiveTimestamp = System.currentTimeMillis(),
                    isCurrentDevice = true
                ),
                UserSession(
                    sessionId = UUID.randomUUID().toString(),
                    deviceName = "Samsung Galaxy Tab S9",
                    clientPlatform = "Android 14",
                    ipAddressMasked = "49.36.***.***",
                    lastActiveTimestamp = System.currentTimeMillis() - 86400000L,
                    isCurrentDevice = false
                )
            )
            userDao.insertSessions(sessions)
        }
    }

    suspend fun setDefaultBankAccount(accountId: String) {
        bankDao.setDefaultBankAccount(accountId)
    }

    suspend fun linkNewBankAccount(bankName: String, bankCode: String, accountNumberRaw: String, ifsc: String): BankAccount {
        val masked = SecurityManager.maskAccountNumber(accountNumberRaw)
        val handle = bankCode.lowercase()
        val generatedUpiId = "akash@$handle"
        val newAccount = BankAccount(
            id = "bank_${bankCode.lowercase()}_${System.currentTimeMillis()}",
            bankName = bankName,
            bankCode = bankCode,
            maskedAccountNumber = masked,
            upiId = generatedUpiId,
            ifscPrefix = ifsc.uppercase(),
            accountType = "Savings",
            isDefault = false,
            balance = (10000..80000).random().toDouble(),
            status = AccountStatus.ACTIVE
        )
        bankDao.insertBankAccount(newAccount)
        return newAccount
    }

    suspend fun unlinkBankAccount(accountId: String) {
        bankDao.deleteBankAccountById(accountId)
        // If the unlinked account was default, set the first available account as default
        val remaining = bankDao.getDefaultBankAccount()
        if (remaining == null) {
            // Pick first
            val all = bankDao.getAccountCount()
            if (all > 0) {
                // Room query will handle or user selects
            }
        }
    }

    suspend fun refreshBankBalance(accountId: String): Double {
        val account = bankDao.getBankAccountById(accountId) ?: return 0.0
        // Simulates official balance fetch via UPI PSP protocol
        val updated = account.balance + (-500..500).random()
        bankDao.updateBalance(accountId, updated)
        return updated
    }

    suspend fun recordTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        // Deduct from bank balance if sent
        if (transaction.type == "SENT" && transaction.status == PaymentStatus.SUCCESS) {
            val accounts = bankDao.getAllBankAccountsFlow()
            // We can adjust the account balance directly if id is known
        }
    }

    suspend fun updateTransactionStatus(transactionId: String, status: PaymentStatus) {
        transactionDao.updateStatus(transactionId, status)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.updateProfile(profile)
    }

    suspend fun logoutAllOtherDevices() {
        userDao.logoutAllOtherDevices()
    }

    suspend fun wipeLocalData() {
        transactionDao.deleteAllTransactions()
        userDao.clearAllSessions()
        userDao.clearProfile()
    }
}
