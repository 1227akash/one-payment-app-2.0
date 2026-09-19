package com.example

import android.app.Application
import com.example.data.OneRepository
import com.example.data.local.OneDatabase
import com.example.domain.ChargeCalculatorService
import com.example.domain.PaymentService
import com.example.domain.PlannerService

class OneApplication : Application() {

    lateinit var database: OneDatabase
        private set

    lateinit var repository: OneRepository
        private set

    lateinit var paymentService: PaymentService
        private set

    lateinit var plannerService: PlannerService
        private set

    lateinit var calculatorService: ChargeCalculatorService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = OneDatabase.getDatabase(this)
        repository = OneRepository(database.bankDao(), database.transactionDao(), database.userDao())
        paymentService = PaymentService(repository)
        plannerService = PlannerService()
        calculatorService = ChargeCalculatorService()
    }

    companion object {
        lateinit var instance: OneApplication
            private set
    }
}
