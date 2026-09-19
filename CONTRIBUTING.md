# Contributing to ONE

Thank you for your interest in contributing to **ONE**!

ONE is built as a transparent, open-source Android digital-payment application designed to uphold high security standards and regulatory compliance with NPCI and RBI standards.

---

## 🛑 Strict Regulatory & Security Boundaries

Before writing code or opening pull requests, please read our core contribution mandates:

1. **NO REGULATION BYPASSING**: PRs that attempt to circumvent MDR charges, evade limits, or manipulate transaction parameters will be rejected immediately.
2. **NO SENSITIVE CREDENTIALS**: Never propose code that requests, stores, transmits, or logs user banking credentials (UPI PIN, debit card PIN, net banking passwords, OTPs).
3. **OFFICIAL SDKs**: Payment processing must always route through certified NPCI/bank PSP mechanisms.

---

## 🛠️ Development Workflow

1. **Fork and Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Coding Standards**:
   - 100% Kotlin with Jetpack Compose (Material 3).
   - Follow MVVM architecture with unidirectional data flow.
   - Respect WCAG touch targets (>= 48dp).
3. **Testing**:
   - Add unit tests in `src/test/` for any new logic or security validation.
   - Run tests:
     ```bash
     gradle :app:testDebugUnitTest
     ```
4. **Submitting Pull Requests**:
   - Provide a clear explanation of changes.
   - Reference any open issues.
