package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.model.BankAccount
import com.example.model.Transaction
import com.example.model.UserProfile
import com.example.model.UserSession

@Database(
    entities = [
        BankAccount::class,
        Transaction::class,
        UserProfile::class,
        UserSession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OneDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: OneDatabase? = null

        fun getDatabase(context: Context): OneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OneDatabase::class.java,
                    "one_payment.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
