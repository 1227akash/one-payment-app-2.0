package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.OneRepository
import com.example.domain.ChargeCalculatorService
import com.example.domain.PaymentExecutionResult
import com.example.domain.PaymentService
import com.example.domain.PlannerService
import com.example.model.BankAccount
import com.example.model.MdrCalculation
import com.example.model.MerchantCategory
import com.example.model.PaymentInstrument
import com.example.model.PaymentMethod
import com.example.model.PaymentPlanResult
import com.example.model.PaymentStatus
import com.example.model.Transaction
import com.example.model.UserProfile
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilterTab {
    ALL, SENT, RECEIVED, PENDING, FAILED, REFUNDED
}

data class PaymentDraft(
    val method: PaymentMethod = PaymentMethod.UPI_ID,
    val recipientIdentifier: String = "",
    val recipientName: String = "",
    val amount: String = "",
    val note: String = "",
    val selectedAccount: BankAccount? = null,
    val verificationError: String? = null,
    val isVerified: Boolean = false,
    val isAuthorizing: Boolean = false,
    val executionResult: PaymentExecutionResult? = null
)

class OneViewModel(
    private val repository: OneRepository,
    private val paymentService: PaymentService,
    private val plannerService: PlannerService,
    private val calculatorService: ChargeCalculatorService
) : ViewModel() {

    val bankAccounts: StateFlow<List<BankAccount>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultAccount: StateFlow<BankAccount?> = repository.defaultBankAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _rawTransactions = repository.allTransactions

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSessions: StateFlow<List<UserSession>> = repository.activeSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History filtering
    val historyFilterTab = MutableStateFlow(HistoryFilterTab.ALL)
    val historySearchQuery = MutableStateFlow("")

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _rawTransactions,
        historyFilterTab,
        historySearchQuery
    ) { transactions, tab, query ->
        transactions.filter { tx ->
            val matchesTab = when (tab) {
                HistoryFilterTab.ALL -> true
                HistoryFilterTab.SENT -> tx.type == "SENT"
                HistoryFilterTab.RECEIVED -> tx.type == "RECEIVED"
                HistoryFilterTab.PENDING -> tx.status == PaymentStatus.PENDING
                HistoryFilterTab.FAILED -> tx.status == PaymentStatus.FAILED
                HistoryFilterTab.REFUNDED -> tx.status == PaymentStatus.REFUNDED
            }
            val matchesQuery = if (query.isBlank()) true else {
                tx.recipientOrSenderName.contains(query, ignoreCase = true) ||
                        tx.recipientOrSenderUpiId.contains(query, ignoreCase = true) ||
                        tx.utrReference.contains(query, ignoreCase = true) ||
                        tx.note.contains(query, ignoreCase = true)
            }
            matchesTab && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payment Draft & Execution State
    private val _paymentDraft = MutableStateFlow(PaymentDraft())
    val paymentDraft: StateFlow<PaymentDraft> = _paymentDraft.asStateFlow()

    // Smart Planner State
    private val _plannerAmount = MutableStateFlow("5000")
    val plannerAmount: StateFlow<String> = _plannerAmount.asStateFlow()

    private val _plannerResult = MutableStateFlow<PaymentPlanResult?>(null)
    val plannerResult: StateFlow<PaymentPlanResult?> = _plannerResult.asStateFlow()

    // Charge Calculator State
    val calcAmount = MutableStateFlow("2500")
    val calcCategory = MutableStateFlow(MerchantCategory.LARGE_MERCHANT)
    val calcInstrument = MutableStateFlow(PaymentInstrument.BANK_UPI)
    val calcResult: StateFlow<MdrCalculation> = combine(
        calcAmount,
        calcCategory,
        calcInstrument
    ) { amountStr, cat, inst ->
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        calculatorService.calculate(amount, cat, inst)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        calculatorService.calculate(2500.0, MerchantCategory.LARGE_MERCHANT, PaymentInstrument.BANK_UPI)
    )

    // Auth State
    val isAuthenticated = MutableStateFlow(true)
    val appLockEnabled = MutableStateFlow(true)
    val isAppUnlocked = MutableStateFlow(true)

    init {
        // Initialize planner with default
        generatePaymentPlan("5000")
    }

    // Payment Draft Actions
    fun setPaymentMethod(method: PaymentMethod) {
        _paymentDraft.value = _paymentDraft.value.copy(
            method = method,
            verificationError = null,
            isVerified = false,
            executionResult = null
        )
    }

    fun setRecipientIdentifier(identifier: String) {
        _paymentDraft.value = _paymentDraft.value.copy(
            recipientIdentifier = identifier,
            verificationError = null
        )
    }

    fun verifyRecipient() {
        val draft = _paymentDraft.value
        val result = paymentService.verifyRecipient(draft.recipientIdentifier, draft.method)
        result.onSuccess { name ->
            val defaultAcc = defaultAccount.value ?: bankAccounts.value.firstOrNull()
            _paymentDraft.value = draft.copy(
                recipientName = name,
                isVerified = true,
                verificationError = null,
                selectedAccount = draft.selectedAccount ?: defaultAcc
            )
        }.onFailure { err ->
            _paymentDraft.value = draft.copy(
                verificationError = err.message ?: "Verification failed",
                isVerified = false
            )
        }
    }

    fun setPaymentAmount(amount: String) {
        _paymentDraft.value = _paymentDraft.value.copy(amount = amount)
    }

    fun setPaymentNote(note: String) {
        _paymentDraft.value = _paymentDraft.value.copy(note = note)
    }

    fun selectSourceAccount(account: BankAccount) {
        _paymentDraft.value = _paymentDraft.value.copy(selectedAccount = account)
    }

    fun executePayment(
        forceSimulatedFailure: Boolean = false,
        forceSimulatedUncertainty: Boolean = false
    ) {
        val draft = _paymentDraft.value
        val account = draft.selectedAccount ?: defaultAccount.value ?: bankAccounts.value.firstOrNull()
        if (account == null) {
            _paymentDraft.value = draft.copy(
                verificationError = "Please select a bank account to pay from."
            )
            return
        }

        val parsedAmount = draft.amount.toDoubleOrNull() ?: 0.0
        _paymentDraft.value = draft.copy(isAuthorizing = true)

        viewModelScope.launch {
            val result = paymentService.executePayment(
                sourceAccount = account,
                recipientIdentifier = draft.recipientIdentifier,
                recipientName = draft.recipientName,
                amount = parsedAmount,
                method = draft.method,
                note = draft.note,
                forceSimulatedFailure = forceSimulatedFailure,
                forceSimulatedUncertainty = forceSimulatedUncertainty
            )
            _paymentDraft.value = _paymentDraft.value.copy(
                isAuthorizing = false,
                executionResult = result
            )
        }
    }

    fun resetPaymentDraft() {
        val defaultAcc = defaultAccount.value ?: bankAccounts.value.firstOrNull()
        _paymentDraft.value = PaymentDraft(selectedAccount = defaultAcc)
    }

    // Bank Account Management
    fun setDefaultAccount(accountId: String) {
        viewModelScope.launch {
            repository.setDefaultBankAccount(accountId)
        }
    }

    fun linkNewBankAccount(bankName: String, bankCode: String, rawAccountNumber: String, ifsc: String) {
        viewModelScope.launch {
            repository.linkNewBankAccount(bankName, bankCode, rawAccountNumber, ifsc)
        }
    }

    fun unlinkAccount(accountId: String) {
        viewModelScope.launch {
            repository.unlinkBankAccount(accountId)
        }
    }

    fun refreshBalance(accountId: String) {
        viewModelScope.launch {
            repository.refreshBankBalance(accountId)
        }
    }

    // Smart Planner
    fun updatePlannerAmount(amount: String) {
        _plannerAmount.value = amount
        generatePaymentPlan(amount)
    }

    fun generatePaymentPlan(amountStr: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val plan = plannerService.createPaymentPlan(amount, bankAccounts.value)
        _plannerResult.value = plan
    }

    // Settings & Security
    fun toggleBiometricLock(enabled: Boolean) {
        appLockEnabled.value = enabled
        userProfile.value?.let { profile ->
            viewModelScope.launch {
                repository.updateUserProfile(profile.copy(biometricEnabled = enabled))
            }
        }
    }

    fun setLanguage(lang: String) {
        userProfile.value?.let { profile ->
            viewModelScope.launch {
                repository.updateUserProfile(profile.copy(preferredLanguage = lang))
            }
        }
    }

    fun logoutAllOtherDevices() {
        viewModelScope.launch {
            repository.logoutAllOtherDevices()
        }
    }

    fun wipeLocalData() {
        viewModelScope.launch {
            repository.wipeLocalData()
        }
    }

    companion object {
        fun factory(
            repository: OneRepository,
            paymentService: PaymentService,
            plannerService: PlannerService,
            calculatorService: ChargeCalculatorService
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OneViewModel(repository, paymentService, plannerService, calculatorService) as T
                }
            }
        }
    }
}
