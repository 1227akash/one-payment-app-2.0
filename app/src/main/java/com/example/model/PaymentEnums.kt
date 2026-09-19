package com.example.model

enum class PaymentMethod(val displayName: String) {
    QR("QR Code"),
    UPI_ID("UPI ID"),
    MOBILE("Mobile Number"),
    BANK_IFSC("Bank Account & IFSC")
}

enum class PaymentStatus(val label: String) {
    SUCCESS("SUCCESS"),
    PENDING("PENDING"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED"),
    REFUNDED("REFUNDED"),
    UNKNOWN("VERIFICATION REQUIRED")
}

enum class AccountStatus {
    ACTIVE,
    KYC_VERIFIED,
    RESTRICTED
}

enum class MerchantCategory(val label: String, val baseRate: Double, val cap: Double?) {
    P2P("Peer-to-Peer (Personal)", 0.0, 0.0),
    SMALL_OFFLINE("Small Offline Merchant (< ₹2000)", 0.0, 0.0),
    LARGE_MERCHANT("Standard Merchant (P2M)", 0.009, 100.0),
    FUEL_AND_GOVT("Fuel & Government Payments", 0.005, 50.0),
    UTILITY_BILLS("Utility & Telecom Bills", 0.007, 75.0),
    WALLET_PPI("Wallet / PPI on UPI (> ₹2000)", 0.011, 250.0)
}

enum class PaymentInstrument(val label: String) {
    BANK_UPI("Bank Account (UPI)"),
    RUPAY_CREDIT("RuPay Credit Card on UPI"),
    PPI_WALLET("Prepaid Wallet (PPI)")
}
