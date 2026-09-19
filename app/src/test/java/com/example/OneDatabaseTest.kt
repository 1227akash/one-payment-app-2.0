package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BankDao
import com.example.data.local.OneDatabase
import com.example.data.local.TransactionDao
import com.example.model.AccountStatus
import com.example.model.BankAccount
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Transaction
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OneDatabaseTest {

    private lateinit var db: OneDatabase
    private lateinit var bankDao: BankDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OneDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bankDao = db.bankDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndSetDefaultBankAccount() = runBlocking {
        val account1 = BankAccount(
            id = "b1",
            bankName = "HDFC Bank",
            bankCode = "HDFC",
            maskedAccountNumber = "XXXX XXXX 4582",
            upiId = "akash@okhdfcbank",
            ifscPrefix = "HDFC0001",
            isDefault = true
        )
        val account2 = BankAccount(
            id = "b2",
            bankName = "State Bank of India",
            bankCode = "SBI",
            maskedAccountNumber = "XXXX XXXX 9217",
            upiId = "akash@oksbi",
            ifscPrefix = "SBIN0002",
            isDefault = false
        )

        bankDao.insertBankAccount(account1)
        bankDao.insertBankAccount(account2)

        assertEquals(2, bankDao.getAccountCount())
        val defaultBefore = bankDao.getDefaultBankAccount()
        assertNotNull(defaultBefore)
        assertEquals("b1", defaultBefore?.id)

        // Switch default to account 2
        bankDao.setDefaultBankAccount("b2")

        val defaultAfter = bankDao.getDefaultBankAccount()
        assertEquals("b2", defaultAfter?.id)
    }

    @Test
    fun testInsertAndQueryTransactions() = runBlocking {
        val tx = Transaction(
            id = "tx_123",
            utrReference = "328912449102",
            amount = 1500.0,
            fee = 0.0,
            type = "SENT",
            status = PaymentStatus.SUCCESS,
            recipientOrSenderName = "Blue Tokai Coffee",
            recipientOrSenderUpiId = "bluetokai@icici",
            sourceBankName = "HDFC Bank",
            sourceAccountMasked = "XXXX XXXX 4582",
            method = PaymentMethod.QR
        )

        transactionDao.insertTransaction(tx)
        assertEquals(1, transactionDao.getTransactionCount())

        val fetched = transactionDao.getTransactionById("tx_123")
        assertNotNull(fetched)
        assertEquals("328912449102", fetched?.utrReference)
        assertEquals(1500.0, fetched?.amount ?: 0.0, 0.001)
        assertEquals(PaymentStatus.SUCCESS, fetched?.status)
    }
}
