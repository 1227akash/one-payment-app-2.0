# Security Policy

## 🔒 Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

---

## 🛡️ Security Architecture & Principles

ONE is engineered with defense-in-depth security principles:

1. **Hardware-Isolated Android Keystore**: All local sensitive keys and tokens are encrypted using AES-256 GCM authenticated encryption.
2. **Zero Sensitive Credential Ingestion**: The application never touches or requests UPI PINs, card CVVs, or bank passwords.
3. **Idempotency Protection**: Every payment dispatch generates a SHA-256 cryptographically secure idempotency key to prevent double debits.
4. **Biometric Authentication**: Biometric verification (Class 3 strong biometrics) protects user sessions.
5. **PII Masking**: Bank account numbers and UPI handles are masked in user interfaces.

---

## 📢 Reporting a Vulnerability

If you discover a potential security vulnerability in ONE, please report it privately:

- **Email**: `security@onepayment.example.org`
- **PGP Key**: Available upon request
- Please include:
  - Description of the vulnerability
  - Steps to reproduce / Proof of Concept
  - Impact assessment

We commit to acknowledging your report within 48 hours and providing a remediation timeline.
