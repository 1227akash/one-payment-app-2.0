# ONE — Smart Payment & Payment Planning App

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-blue.svg)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)]()

**ONE** is an open-source, production-grade, secure Android digital payment and payment planning application engineered to deliver a seamless UPI payment experience compliant with RBI (Reserve Bank of India) and NPCI (National Payments Corporation of India) regulations.

---

## 🏛️ Core Principles & Regulatory Compliance

1. **Strict Legal & Banking Compliance**: ONE is strictly engineered **NOT** to evade, bypass, manipulate, or circumvent UPI/MDR charges, banking regulations, transaction limits, fraud detection systems, or KYC requirements.
2. **Zero Sensitive Credential Storage**: ONE **never** collects, intercepts, logs, or stores sensitive financial credentials, including:
   - UPI PINs
   - ATM / Debit Card PINs
   - Card CVVs / CVCs
   - Net Banking Passwords
   - One-Time Passwords (OTPs)
3. **Official PSP & Gateway Authorization**: All actual money movements and payment finalizations are executed exclusively through official certified Bank and NPCI SDK channels.

---

## ✨ Features

- **Multi-Bank Account Management**: Link multiple supported bank accounts (HDFC, SBI, ICICI, Axis, Kotak, PNB, etc.), select default debit accounts, switch default sources, and refresh real-time balances.
- **Unified Payment Engine**:
  - **Scan & Pay**: Real-time QR code scanner with automatic UPI URI parsing (`upi://pay?pa=...&pn=...&am=...`).
  - **UPI ID Transfer**: Direct transfer to any verified UPI VPA handle.
  - **Mobile Number Transfer**: Direct peer-to-peer transfer to verified 10-digit Indian phone numbers.
  - **Bank Account & IFSC**: Direct transfer via Account Number and Indian Financial System Code.
- **Resilient Status & Uncertainty Handling**: If payment status is indeterminate or network times out, transactions are safely flagged with verification notices to prevent double-spending.
- **Smart Payment Planner**: Calculate legitimate split payment schedules across linked bank accounts according to provider rules. Prominently displays the mandatory statutory notice:
  > *"Splitting a payment does not guarantee that applicable charges will be avoided. Every transaction must follow official provider rules and requires explicit user authorization."*
- **Regulatory UPI Charge & MDR Calculator**: Real-time breakdown of interchange fees, merchant categories (P2P, Small Offline, Large P2M, Fuel/Govt, Utilities, Wallet/PPI), 18% GST calculation, and statutory caps.
- **Security & Privacy**:
  - Hardware-backed encryption via Android Keystore (AES-256 GCM).
  - SHA-256 idempotency key generation to prevent accidental double-clicks or replays.
  - Biometric authentication (Fingerprint / Face / Screen Lock) via `androidx.biometric.BiometricPrompt`.
  - Account number masking (e.g. `XXXX XXXX 4582`).
  - Complete local data erasure (Wipe Local Data) for full user control.
- **Accessibility & Multilingual Support**:
  - Fully accessible UI adhering to WCAG touch targets (>= 48dp).
  - High-contrast Material 3 theme.
  - Localization in English and Hindi (`values-hi/strings.xml`).

---

## 🏗️ Architecture & Technology Stack

```
com.example
├── OneApplication.kt           # Application singleton & dependency initialization
├── MainActivity.kt             # Navigation host & edge-to-edge entry point
├── model/                      # Domain & Room persistence entities
│   ├── BankAccount.kt
│   ├── Transaction.kt
│   ├── PaymentEnums.kt
│   ├── PlannedSplit.kt
│   ├── MdrCalculation.kt
│   ├── UserProfile.kt
│   └── UserSession.kt
├── security/                   # Security, Keystore encryption & validation
│   ├── SecurityManager.kt
│   └── BiometricHelper.kt
├── data/                       # Room persistence layer
│   ├── local/
│   │   ├── BankDao.kt
│   │   ├── TransactionDao.kt
│   │   ├── UserDao.kt
│   │   ├── Converters.kt
│   │   └── OneDatabase.kt
│   └── OneRepository.kt
├── domain/                     # Business logic services
│   ├── PaymentService.kt
│   ├── PlannerService.kt
│   └── ChargeCalculatorService.kt
├── ui/
│   ├── theme/                  # Material 3 typography, colors & themes
│   ├── components/             # Reusable TopBars, Status chips, and Avatars
│   ├── screens/                # Jetpack Compose UI screens
│   └── viewmodel/              # Reactive MVVM StateFlow ViewModel
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 or JDK 21
- Android SDK 35 (compileSdk 36, minSdk 24)

### Building the Project
```bash
# Clone the repository
git clone https://github.com/example/one-payment-app.git
cd one-payment-app

# Build debug APK
gradle assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest
```

---

## 🔒 Security & Vulnerability Reporting

Please review [SECURITY.md](SECURITY.md) for vulnerability disclosure procedures and detailed security policies.

---

## 📄 License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
