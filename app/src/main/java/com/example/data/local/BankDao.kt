package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.BankAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT * FROM bank_accounts ORDER BY isDefault DESC, bankName ASC")
    fun getAllBankAccountsFlow(): Flow<List<BankAccount>>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getBankAccountById(id: String): BankAccount?

    @Query("SELECT * FROM bank_accounts WHERE isDefault = 1 LIMIT 1")
    fun getDefaultBankAccountFlow(): Flow<BankAccount?>

    @Query("SELECT * FROM bank_accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBankAccount(): BankAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(account: BankAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccounts(accounts: List<BankAccount>)

    @Update
    suspend fun updateBankAccount(account: BankAccount)

    @Delete
    suspend fun deleteBankAccount(account: BankAccount)

    @Query("DELETE FROM bank_accounts WHERE id = :id")
    suspend fun deleteBankAccountById(id: String)

    @Query("UPDATE bank_accounts SET isDefault = 0")
    suspend fun clearDefaultBankAccounts()

    @Query("UPDATE bank_accounts SET isDefault = 1 WHERE id = :id")
    suspend fun markAccountAsDefault(id: String)

    @Transaction
    suspend fun setDefaultBankAccount(id: String) {
        clearDefaultBankAccounts()
        markAccountAsDefault(id)
    }

    @Query("UPDATE bank_accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(id: String, newBalance: Double)

    @Query("SELECT COUNT(*) FROM bank_accounts")
    suspend fun getAccountCount(): Int
}
