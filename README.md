# Multi-Factor Authentication (2FA / TOTP) Banking System (`bankSystem`)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-MFA%20%7C%20RFC%206238%20TOTP-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![Cybersecurity](https://img.shields.io/badge/Security-BCrypt%20%7C%20Custom%20Filters%20%7C%20Hierarchical%20RBAC-red?style=flat)

## Overview
An advanced **Spring Boot** banking application focused on **Identity & Access Management (IAM)**, cryptographic authentication, and custom HTTP security filter chains. Implements **Time-based One-Time Passwords (TOTP – RFC 6238)** from scratch with QR code provisioning, **BCrypt** password hashing, **Hierarchical Role-Based Access Control (RBAC)**, and multi-chain `SecurityFilterChain` pipelines.

---

## Key Security & Engineering Highlights
- **RFC 6238 TOTP Two-Factor Authentication (2FA) from Scratch**:
  - Custom `Base32Encoder` (RFC 4648) and `TotpUtil` implementing `HmacSHA1` dynamic truncation, 160-bit `SecureRandom` secret generation, and configurable time-step skew tolerance.
  - Dynamic **QR Code PNG generation** (via Google ZXing) encoded as Base64 data URIs for seamless enrollment in Google Authenticator / Authy.
  - Dual support for **Thymeleaf Web UI** (`/totp-setup`, `/totp-disable`) and **REST API** (`/api/totp/setup`, `/api/totp/verify`, `/api/auth/login`).
- **Multi-Chain Spring Security Architecture**:
  - **Chain 1 (`@Order(1)` – `/api/filter/**`)**: Custom servlet security filters (`OncePerRequestFilter`) including `CustomAuthenticationFilter` (token extraction & `SecurityContextHolder` population), `CustomHeaderFilter` (`X-API-KEY` verification), and `IpLoggingFilter` (client IP extraction and telemetry).
  - **Chain 2 (Web & Auth Security)**: `DaoAuthenticationProvider` with `BCryptPasswordEncoder`, custom `CustomUserDetailsService` enforcing TOTP verification during login, `CustomAuthenticationFailureHandler`, and `RoleHierarchy` (`ROLE_ADMIN` > `ROLE_MODERATOR` > `ROLE_USER`).
- **Core Banking & Admin Management**:
  - IBAN (`PL` + 26 digits) and PESEL validation, deposits, withdrawals, peer-to-peer transfers by PESEL, and transaction auditing.
  - Admin/Moderator user management panel (`/management/getAll`) with dynamic role toggling and account administration.

---

## Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.4.5 (Spring Security, Spring Web, Spring Validation, Thymeleaf)
- **Cryptography & MFA:** `javax.crypto.Mac` (`HmacSHA1`), `SecureRandom`, BCrypt, Custom RFC 4648 Base32, Google ZXing (QR Codes)
- **Utilities:** Lombok, Jackson JSR-310, Apache HttpClient 5

---

## Key Endpoints

| Category | Endpoint | Description |
| :--- | :--- | :--- |
| **Web Auth** | `GET/POST /login`, `GET/POST /register` | Form-based authentication with 2FA TOTP support & user registration |
| **2FA Web UI** | `GET/POST /totp-setup`, `POST /totp-disable` | Interactive QR code 2FA enrollment and verification |
| **2FA REST API** | `POST /api/totp/setup`, `POST /api/auth/login` | Programmatic TOTP provisioning and 2FA REST authentication |
| **Banking** | `POST /deposit`, `POST /withdraw`, `POST /transfer` | Authenticated customer financial operations (`ROLE_USER`) |
| **Admin** | `GET /management/getAll`, `POST /management/toggleRole` | Hierarchical RBAC user administration (`ROLE_ADMIN` / `ROLE_MODERATOR`) |
| **Security Filters** | `/api/filter/example1`, `/api/filter/example2` | Custom `OncePerRequestFilter` chain (`X-API-KEY` & IP audit logging) |

---

## Running Locally

```bash
cd bankSystem

# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```
