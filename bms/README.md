# Banking Management System (BMS)

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-RBAC-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template_Engine-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?style=for-the-badge)
![H2](https://img.shields.io/badge/H2-In--Memory_DB-003B6F?style=for-the-badge)
![BCrypt](https://img.shields.io/badge/BCrypt-Password_Hashing-red?style=for-the-badge)
![OOAD](https://img.shields.io/badge/Design-OOAD%20%7C%20UML-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=for-the-badge)

---

## Overview

The **Banking Management System (BMS)** is a full-stack web application built with **Spring Boot 3.2.0**, implementing a complete, production-structured banking platform with four distinct user roles. It was developed following a rigorous **Object-Oriented Analysis and Design (OOAD)** methodology, covering the full software engineering lifecycle — problem analysis, use case modeling, statechart diagrams, sequence diagrams, activity diagrams, class diagrams, and component/deployment/package diagrams — all translated directly into working Java code.

The system enforces **role-based access control** (RBAC) via Spring Security, supports a full **loan lifecycle** from application to disbursement, implements a **transactional state machine** for all financial operations, and automatically seeds demonstration data on first run. All passwords are BCrypt-hashed. Data is persisted via H2 in-memory JPA.

Developed for **BITE404E — Object Oriented Analysis and Design Lab**, VIT University.

---

## Features

### Customer
- Self-registration with email, phone, address, date of birth, and nationality
- BCrypt-authenticated login with account lock after repeated failures
- Dashboard showing total active balance across all accounts
- Create **Savings**, **Current**, **Fixed Deposit**, or **Recurring Deposit** accounts with a minimum initial deposit of ₹500
- Deposit, withdraw (min ₹1), and transfer funds between accounts
- Transfer includes same-account guard — cannot transfer to self
- Per-account detail page with full transaction history (ordered by date, newest first)
- Apply for loans: HOME, CAR, PERSONAL, EDUCATION — with type, amount, interest rate, duration (1–360 months), and purpose
- View loan status, EMI calculation, total payable, and total interest on the loan detail page
- Profile view page

### Bank Staff
- Dashboard with total accounts, loans, and customer count + recent 8 accounts
- View all bank accounts across all customers
- View complete customer list

### Manager
- Dashboard with pending and under-review loan counts + recent 5 pending loans
- View all loan applications
- Mark loans as **Under Review**
- **Approve** or **Reject** loans with mandatory remarks (manager identity tied via `Authentication`)
- **Disburse** approved loans (enforces APPROVED-only guard)
- View all customer accounts

### Admin
- Dashboard with system-wide totals: users, accounts, loans + recent 5 users and 5 accounts
- Create staff users (Bank Staff, Manager, Admin) with role-specific fields
- View all registered users
- **Deactivate**, **Activate**, and **Unlock** any user account
- **Freeze** or **Activate** any bank account
- View all loans system-wide

---

## OOAD Design

This project was built on a complete OOAD process documented in the project report. Every UML artifact was translated directly into working Spring Boot code.

---

### Problem Analysis

Traditional banking systems rely on fragmented, partially manual workflows that suffer from slow processing, human error, poor UX, and critical security gaps — specifically lacking strong authentication, role-based access control, and secure handling of sensitive financial data. The BMS replaces these with a centralized, web-based, role-driven platform built on OOP principles of modularity, reusability, and strict layered architecture: **Presentation → Business Logic → Data**.

---

### Actors & Role Mapping

| Actor | Responsibilities | Spring Security Role | URL Scope |
|---|---|---|---|
| Customer | Transactions, loan applications, account management | `ROLE_CUSTOMER` | `/customer/**` |
| Bank Staff | View accounts and customer data | `ROLE_BANK_STAFF` | `/staff/**` |
| Manager | Loan review, approval, rejection, disbursement | `ROLE_MANAGER` | `/manager/**` |
| Admin | User creation, role assignment, account control | `ROLE_ADMIN` | `/admin/**` |

---

### Use Case Relationships (UML)

**«include»** — Mandatory sub-flows reused across use cases:
- `Login` includes `Authenticate User`
- `Transfer Money` includes `Check Account Balance` and `Validate Beneficiary`
- `Apply for Loan` includes `Enter Loan Details` and `Verify Customer`

**«extend»** — Conditional behavior triggered only under specific circumstances:
- `Login` → `Display Error Message` (invalid credentials → `/login?error=true`)
- `Transfer Money` → `Transaction Failure` (insufficient balance → `fail(reason)` on Transaction)
- `Apply for Loan` → `Loan Rejection` (manager rejects → `REJECTED` status with remarks)

**Generalization (Inheritance)**:
- `Transaction` → `DEPOSIT`, `WITHDRAW`, `TRANSFER` (via `TransactionType` enum + single `Transaction` entity)
- `User` → `Customer`, `BankStaff`, `Manager`, `Admin` (JPA `SINGLE_TABLE` inheritance with `@DiscriminatorValue`)

---

### Class Hierarchy (Implemented)

```
                    User  (abstract JPA @Entity — SINGLE_TABLE inheritance)
                   / |  \  \
          Customer  |  BankStaff  Manager  Admin
                    |
   (all stored in 'users' table, discriminated by 'user_type' column)

User fields:
  userId (PK), name, email (unique), password (BCrypt),
  role (enum), active, loginAttempts, locked,
  createdAt, lastLogin, phone, address

User methods (UML):
  login(rawPassword) — validates, locks after 3 failures
  logout()           — updates lastLogin
  updateProfile()    — name, phone, address
  changePassword()   — sets new password

Customer adds:   customerCode, dateOfBirth, nationality
                 @OneToMany(CASCADE ALL, LAZY) → List<Account>
                 @OneToMany(CASCADE ALL, LAZY) → List<Loan>

BankStaff adds:  employeeId, department, designation

Manager adds:    employeeId, branchCode
                 @OneToMany(LAZY) → List<Loan> (reviewedLoans)

Admin adds:      adminLevel, department
```

```
Account (@Entity, table: accounts)
  accountId (PK), accountNumber (unique), accountType (enum),
  balance (BigDecimal 15,2), status (enum), createdDate,
  lastTransactionDate
  @ManyToOne(LAZY)   → Customer
  @OneToMany(CASCADE ALL, LAZY, ORDER BY transactionDate DESC) → List<Transaction>

  Methods (UML):
    deposit(amount)    — enforces ACTIVE status, positive amount
    withdraw(amount)   — enforces ACTIVE status, positive amount, sufficient balance
                         sets status → OVERDRAWN if balance goes negative
    transfer(amount)   — delegates to withdraw() (debit side)
    checkBalance()     — returns current balance
    freezeAccount()    — status → FROZEN
    closeAccount()     — status → CLOSED
    activate()         — status → ACTIVE

Transaction (@Entity, table: transactions)
  transactionId (PK), transactionReference (unique), transactionType (enum),
  amount (BigDecimal 15,2), balanceAfter, status (enum),
  transactionDate, description, failureReason, targetAccountNumber
  @ManyToOne(LAZY) → Account

  State machine methods:
    initiate() → INITIATED
    validate() → VALIDATING
    approve()  → APPROVED
    process()  → PROCESSING
    complete() → COMPLETED
    fail(reason) → FAILED + sets failureReason

Loan (@Entity, table: loans)
  loanId (PK), loanReference (unique), loanType (String),
  loanAmount (BigDecimal 15,2), interestRate (BigDecimal 5,2),
  durationMonths, status (enum), purpose, remarks,
  appliedDate, reviewedDate, disbursedDate
  @ManyToOne(LAZY) → Customer
  @ManyToOne(LAZY) → Manager (reviewedBy)

  Methods (UML):
    applyLoan()              — DRAFT → SUBMITTED
    approveLoan(mgr, notes)  — → APPROVED, sets reviewedBy + remarks
    rejectLoan(mgr, notes)   — → REJECTED, sets reviewedBy + remarks
    disburse()               — → DISBURSED, sets disbursedDate
    calculateEMI()           — P*r*(1+r)^n / ((1+r)^n - 1)
    getTotalPayable()        — EMI × durationMonths
    getTotalInterest()       — totalPayable − loanAmount
```

---

### Enumerations

| Enum | Values |
|---|---|
| `Role` | `CUSTOMER`, `BANK_STAFF`, `MANAGER`, `ADMIN` |
| `AccountType` | `SAVINGS`, `CURRENT`, `FIXED_DEPOSIT`, `RECURRING_DEPOSIT` |
| `AccountStatus` | `CREATED`, `ACTIVE`, `FROZEN`, `DORMANT`, `CLOSED`, `OVERDRAWN` |
| `TransactionType` | `DEPOSIT`, `WITHDRAW`, `TRANSFER` |
| `TransactionStatus` | `INITIATED`, `VALIDATING`, `APPROVED`, `PROCESSING`, `COMPLETED`, `FAILED` |
| `LoanStatus` | `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `DISBURSED`, `CLOSED` |

---

### Statechart Diagrams (All 4 Modules)

**Account — Account Management Module**

| From | Stimulus | Guard | Response | To |
|---|---|---|---|---|
| CREATED | activateAccount() | — | setStatus(ACTIVE) | ACTIVE |
| ACTIVE | inactivityDetected() | — | markDormant() | DORMANT |
| DORMANT | reactivate() | — | restorePrivileges() | ACTIVE |
| ACTIVE | closeAccount() | — | archiveAccount() | CLOSED |
| ACTIVE | freezeAccount() | — | setStatus(FROZEN) | FROZEN |
| FROZEN | unfreezeAccount() | — | restoreAccess() | ACTIVE |
| ACTIVE | withdraw() | balance < 0 | notifyCustomer() | OVERDRAWN |
| OVERDRAWN | deposit() | balance ≥ 0 | updateBalance() | ACTIVE |

**Transaction — Transaction Management Module**

| From | Stimulus | Guard | Response | To |
|---|---|---|---|---|
| — | buildTransaction() | — | setStatus(INITIATED) | INITIATED |
| INITIATED | validate() | — | checkRules() | VALIDATING |
| VALIDATING | approve() | valid | markApproved() | APPROVED |
| VALIDATING | fail(reason) | invalid | logError() | FAILED |
| APPROVED | process() | — | debitOrCredit() | PROCESSING |
| PROCESSING | complete() | success | generateReceipt() | COMPLETED |
| PROCESSING | fail(reason) | error | rollback() | FAILED |

**Loan — Loan Management Module**

| From | Stimulus | Guard | Response | To |
|---|---|---|---|---|
| DRAFT | applyLoan() | — | notifyManager() | SUBMITTED |
| SUBMITTED | markUnderReview() | — | verifyDocuments() | UNDER_REVIEW |
| UNDER_REVIEW | approveLoan() | riskLow | setStatus(APPROVED) | APPROVED |
| UNDER_REVIEW | rejectLoan() | riskHigh | setStatus(REJECTED) | REJECTED |
| APPROVED | disburseLoan() | — | transferFunds() | DISBURSED |
| DISBURSED | repayComplete() | — | closeLoan() | CLOSED |

**User — Administration Module**

| From | Stimulus | Guard | Response | To |
|---|---|---|---|---|
| NEW_USER | activateUser() | — | sendCredentials() | ACTIVE |
| ACTIVE | — | — | — | LOGGED_OUT |
| LOGGED_OUT | login() | valid | createSession() | LOGGED_IN |
| LOGGED_OUT | login() | attempts > 3 | lockAccount() | LOCKED |
| LOGGED_IN | logout() | — | destroySession() | LOGGED_OUT |
| LOGGED_IN | violationDetected() | — | suspendAccess() | SUSPENDED |
| ACTIVE | deactivateUser() | — | disableAccount() | DEACTIVATED |
| LOCKED | resetPassword() | — | unlockAccount() | ACTIVE |

---

### Sequence Diagrams

**Login Sequence:**
```
Browser → AuthController.loginPage()  →  auth/login template
User submits → Spring Security /login (POST)
  → SecurityConfig.userDetailsService().loadUserByUsername(email)
  → UserRepository.findByEmail(email)
  → check isActive()  → false → UsernameNotFoundException("Account is deactivated")
  → check isLocked()  → true  → UsernameNotFoundException("Account is locked")
  → BCrypt.matches(raw, encoded)
  → alt [valid]   → createSession → /dashboard
                     → role switch:
                       ROLE_ADMIN      → /admin/dashboard
                       ROLE_MANAGER    → /manager/dashboard
                       ROLE_BANK_STAFF → /staff/dashboard
                       ROLE_CUSTOMER   → /customer/dashboard
     [invalid]   → /login?error=true
  → loop  retry login
  → break [attempts >= 3] → user.setLocked(true)
```

**Transfer Money Sequence:**
```
Customer → CustomerController.transfer()
  → AccountService.transfer(fromAccNo, toAccNo, amount, desc)
  → getAccountByNumber(from) + getAccountByNumber(to)
  → guard: from.equals(to) → IllegalArgumentException("Cannot transfer to same account")
  → buildTransaction(from, TRANSFER) → status = INITIATED
  → buildTransaction(to,   DEPOSIT)  → status = INITIATED
  → debitTxn.validate() → VALIDATING
  → debitTxn.approve()  → APPROVED
  → debitTxn.process()  → PROCESSING
  → creditTxn.validate() → approve() → process()
  → par:
      fromAccount.transfer(amount) → Account.withdraw(amount)
        → guards: positive amount, ACTIVE status, sufficient balance
      toAccount.deposit(amount)
        → guards: positive amount, ACTIVE status
  → debitTxn.setBalanceAfter() → debitTxn.complete()   → COMPLETED
  → creditTxn.setBalanceAfter() → creditTxn.complete()  → COMPLETED
  → @Transactional rollback on any exception
  → critical: both saves or neither (atomic)
  → opt: balanceAfter stored per transaction (receipt)
```

**Loan Approval Sequence:**
```
Customer → CustomerController.applyLoan()
  → LoanService.applyLoan(customer, dto)
  → new Loan() → setStatus(SUBMITTED) → setAppliedDate(now)
  → loanRepository.save() → Ref: LOAN{yyMMdd}{4digits}

Manager → ManagerController.markUnderReview(loanId)
  → LoanService.markUnderReview() → status = UNDER_REVIEW

Manager → ManagerController.approveLoan(loanId, remarks, auth)
  → UserService.getManagerByEmail(auth.getName()) → Manager entity
  → LoanService.approveLoan(loanId, managerId, remarks)
  → Loan.approveLoan(manager, remarks) → APPROVED, reviewedBy = Manager

Manager → ManagerController.disburseLoan(loanId)
  → LoanService.disburseLoan() → guard: status != APPROVED → IllegalStateException
  → Loan.disburse() → DISBURSED, disbursedDate = now()

  → alt [approved]  → disburse loan
     [rejected]     → status = REJECTED, remarks stored
  → loop: review iterations by manager
  → opt: calculateEMI() / getTotalPayable() / getTotalInterest() displayed on detail page
```

---

### Implementation Diagrams

**Component Diagram** — Three tiers with clear dependency direction:
- `Presentation Layer` (Controllers + Thymeleaf templates) → `Business Logic Layer` (Services + DTOs)
- `Business Logic Layer` → `Data Layer` (JPA Repositories + Entities)
- `Spring Security` is a cross-cutting concern intercepting all HTTP requests before reaching controllers

**Deployment Diagram** — Single-node Spring Boot deployment: embedded Tomcat at `localhost:8080`, H2 in-memory database (`jdbc:h2:mem:bmsdb`), Thymeleaf renders HTML server-side, browser communicates via HTTP.

**Package Diagram** — Six packages with directed dependencies:
- `controller` → `service`, `model`, `dto`, `enums`
- `service` → `model`, `repository`, `dto`, `enums`
- `model` → `enums`
- `repository` → `model`
- `config` → `repository`, `model`
- `dto` → `enums`

---

## Project Structure

```
bms/
├── pom.xml                                         # Maven build — Spring Boot 3.2.0, Java 17
│
└── src/main/
    ├── java/com/bms/
    │   ├── BankingManagementSystemApplication.java # @SpringBootApplication entry point
    │   │
    │   ├── config/
    │   │   ├── SecurityConfig.java                 # RBAC, BCrypt, login/logout/H2 config
    │   │   └── DataInitializer.java                # Seeds 4 default users + 2 accounts on boot
    │   │
    │   ├── controller/
    │   │   ├── AuthController.java                 # /, /login, /register, /dashboard (role redirect)
    │   │   ├── CustomerController.java             # /customer/** — accounts, transactions, loans, profile
    │   │   ├── StaffController.java                # /staff/** — accounts, customers view
    │   │   ├── ManagerController.java              # /manager/** — loan lifecycle management
    │   │   └── AdminController.java                # /admin/** — users, accounts, loans, system admin
    │   │
    │   ├── model/
    │   │   ├── User.java                           # Abstract base — SINGLE_TABLE inheritance, login/logout
    │   │   ├── Customer.java                       # @DiscriminatorValue("CUSTOMER")
    │   │   ├── BankStaff.java                      # @DiscriminatorValue("BANK_STAFF")
    │   │   ├── Manager.java                        # @DiscriminatorValue("MANAGER")
    │   │   ├── Admin.java                          # @DiscriminatorValue("ADMIN")
    │   │   ├── Account.java                        # deposit(), withdraw(), freeze(), close()
    │   │   ├── Transaction.java                    # Full state machine: INITIATED → COMPLETED/FAILED
    │   │   └── Loan.java                           # Full lifecycle + calculateEMI(), getTotalPayable()
    │   │
    │   ├── repository/
    │   │   ├── UserRepository.java                 # findByEmail, findByRole, existsByEmail
    │   │   ├── CustomerRepository.java             # findByEmail, findByCustomerCode
    │   │   ├── AccountRepository.java              # findByAccountNumber, findByCustomer, existsByAccountNumber
    │   │   ├── TransactionRepository.java          # findByAccount (full + top10), findByTransactionReference
    │   │   ├── LoanRepository.java                 # findByCustomer, findByStatus, findByCustomerOrderByAppliedDateDesc
    │   │   └── ManagerRepository.java              # findByEmail
    │   │
    │   ├── service/
    │   │   ├── AccountOperations.java              # Interface — deposit(), withdraw(), transfer()
    │   │   ├── AccountService.java                 # Implements AccountOperations — @Transactional
    │   │   ├── LoanService.java                    # Full loan lifecycle — @Transactional
    │   │   └── UserService.java                    # Registration, user CRUD, activate/lock/unlock
    │   │
    │   ├── dto/
    │   │   ├── RegistrationDto.java                # @Email, @Size(min=6), @NotBlank validations
    │   │   ├── UserCreationDto.java                # Role + employeeId/branchCode/department
    │   │   ├── AccountCreationDto.java             # AccountType + @DecimalMin("500.00")
    │   │   ├── TransactionDto.java                 # accountNumber + @DecimalMin("1.00") + optional target
    │   │   └── LoanApplicationDto.java             # @DecimalMin("1000.00"), rate 1–30%, months 1–360
    │   │
    │   └── enums/
    │       ├── Role.java                           # CUSTOMER, BANK_STAFF, MANAGER, ADMIN
    │       ├── AccountType.java                    # SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT
    │       ├── AccountStatus.java                  # CREATED, ACTIVE, FROZEN, DORMANT, CLOSED, OVERDRAWN
    │       ├── TransactionType.java                # DEPOSIT, WITHDRAW, TRANSFER
    │       ├── TransactionStatus.java              # INITIATED, VALIDATING, APPROVED, PROCESSING, COMPLETED, FAILED
    │       └── LoanStatus.java                     # DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED, CLOSED
    │
    └── resources/
        ├── application.properties                  # H2 config, JPA, Thymeleaf, server port
        ├── static/
        │   ├── css/style.css                       # Custom styles
        │   └── js/app.js                           # Frontend JS
        └── templates/
            ├── layout.html                         # Base layout (Thymeleaf fragment)
            ├── fragments.html                      # Reusable navbar/sidebar fragments
            ├── auth/login.html                     # Login page
            ├── auth/register.html                  # Customer self-registration
            ├── admin/                              # dashboard, users, user-create, accounts, loans
            ├── manager/                            # dashboard, loans, loan-detail, accounts
            ├── staff/                              # dashboard, accounts, customers
            └── customer/                           # dashboard, accounts, account-create, account-detail,
                                                    # transactions, loans, loan-apply, loan-detail, profile
```

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Security | Spring Security + BCryptPasswordEncoder | — |
| Persistence | Spring Data JPA (Hibernate) | — |
| Database | H2 In-Memory | — |
| Templating | Thymeleaf + thymeleaf-extras-springsecurity6 | — |
| Validation | Jakarta Bean Validation | — |
| Build | Maven + spring-boot-maven-plugin | — |
| Dev | Spring Boot DevTools | — |

---

## Default Credentials

Seeded automatically by `DataInitializer` on every fresh start. Printed to the console at boot:

```
========================================
  BMS Default Credentials:
  Admin    : admin@bms.com / admin123
  Manager  : manager@bms.com / manager123
  Staff    : staff@bms.com / staff123
  Customer : customer@bms.com / customer123
  URL      : http://localhost:8080
========================================
```

Pre-seeded demo accounts for the customer (Arjun Patel):

| Account Number | Type | Balance |
|---|---|---|
| `BMS260101001` | SAVINGS | ₹25,000.00 |
| `BMS260101002` | CURRENT | ₹75,000.00 |

---

## Installation & Running

### Prerequisites

- **Java 17** or higher — [Download JDK](https://adoptium.net/)
- **Maven 3.6+** — [Download Maven](https://maven.apache.org/download.cgi)

Verify your setup:
```bash
java -version    # should show 17.x.x
mvn -version     # should show 3.6+
```

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/banking-management-system.git
cd banking-management-system/bms
```

---

### Step 2 — Build the Project

```bash
mvn clean install
```

You should see `BUILD SUCCESS` at the end. This compiles all sources, runs any tests, and packages the JAR into `target/`.

---

### Step 3 — Run the Application

**Option A — Maven (recommended for development):**
```bash
mvn spring-boot:run
```

**Option B — JAR directly:**
```bash
java -jar target/banking-management-system-1.0.0.jar
```

---

### Step 4 — Open in Browser

```
http://localhost:8080
```

You will be redirected to `/login`. The default credentials are printed to the terminal.

> The H2 database is in-memory (`create-drop`). All data resets on every restart. This is by design for demonstration.

---

### Step 5 — Access H2 Database Console (Optional)

To inspect the database tables directly during a session:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:bmsdb` |
| Username | `sa` |
| Password | *(leave blank)* |

Click **Connect**. You can then run SQL like `SELECT * FROM USERS` or `SELECT * FROM LOANS`.

---

## Usage Guide

### Test as Customer

1. Login with `customer@bms.com` / `customer123`
2. Dashboard shows total active balance (₹1,00,000 across both pre-seeded accounts)
3. Click **Accounts** → view `BMS260101001` (Savings) and `BMS260101002` (Current)
4. Click an account → see transaction history
5. Go to **Transactions** → try a deposit, withdrawal, or transfer between your two accounts
6. Go to **Loans** → click **Apply for Loan** → fill in HOME / ₹5,00,000 / 8.5% / 120 months
7. Loan detail page shows calculated EMI, total payable, total interest
8. Logout → login as Manager to approve it

### Test as Manager

1. Login with `manager@bms.com` / `manager123`
2. Dashboard shows pending loan count
3. Go to **Loans** → find the loan you just applied for
4. Click **Mark Under Review** → then **Approve** (enter any remarks)
5. Click **Disburse** to complete the lifecycle
6. Try rejecting a loan — enter rejection remarks

### Test as Admin

1. Login with `admin@bms.com` / `admin123`
2. Dashboard shows total users, accounts, loans
3. Go to **Users** → see all 4 seeded users
4. Click **Create User** → create a new Bank Staff with role BANK_STAFF
5. Try **Deactivating** the newly created user → then **Activate** again
6. Go to **Accounts** → **Freeze** one of Arjun's accounts → login as customer and try to withdraw (will fail with "Account is not active")

### Test as Bank Staff

1. Login with `staff@bms.com` / `staff123`
2. Dashboard shows account and customer totals
3. View **Accounts** and **Customers** lists (read-only)

### Test Account Lock

1. Go to `/login` → enter `customer@bms.com` with a wrong password 3 times
2. Account is locked (`loginAttempts >= 3` → `locked = true`)
3. Even the correct password will now fail with "Account is locked"
4. Login as Admin → go to Users → click **Unlock** on the customer
5. Customer can now login normally again

---

## Security Architecture

**Authentication** — Spring Security form login. `SecurityConfig.userDetailsService()` looks up the user by email, checks `isActive()` and `isLocked()`, and constructs a `UserDetails` with authority `ROLE_<ROLENAME>`. All passwords are stored as BCrypt hashes.

**Authorization** — URL-level access control enforced by `SecurityConfig`:

| URL Pattern | Permitted Roles |
|---|---|
| `/`, `/login`, `/register`, `/css/**`, `/js/**`, `/h2-console/**` | Public |
| `/admin/**` | `ROLE_ADMIN` only |
| `/manager/**` | `ROLE_MANAGER`, `ROLE_ADMIN` |
| `/staff/**` | `ROLE_BANK_STAFF`, `ROLE_MANAGER`, `ROLE_ADMIN` |
| `/customer/**` | `ROLE_CUSTOMER`, `ROLE_ADMIN` |
| All others | Any authenticated user |

**Ownership Enforcement** — `CustomerController` validates that the requested account or loan belongs to the authenticated customer before rendering. Accessing another customer's account number redirects silently to the customer's own list.

**Login flow** — Login processing at `POST /login`. Success → `/dashboard` (smart role-based redirect). Failure → `/login?error=true`. Logout at `POST /logout` → `/login?logout=true`.

**CSRF** — Enabled globally; disabled only for `/h2-console/**` to allow the H2 iframe console. `X-Frame-Options: SAMEORIGIN` set to permit H2's frame-based UI.

---

## API / Route Reference

### Auth

| Method | URL | Description |
|---|---|---|
| GET | `/` | Redirect to `/dashboard` if authenticated, else `/login` |
| GET | `/login` | Login page (`?error` / `?logout` query params handled) |
| POST | `/login` | Spring Security credential processing |
| GET | `/register` | Customer registration form |
| POST | `/register` | Submit registration (validated `RegistrationDto`) |
| GET | `/dashboard` | Role-based smart redirect |
| POST | `/logout` | Logout → `/login?logout=true` |

### Customer (`/customer/**`)

| Method | URL | Description |
|---|---|---|
| GET | `/customer/dashboard` | Total balance, account list, loan list |
| GET | `/customer/accounts` | All customer accounts |
| GET | `/customer/accounts/new` | Account creation form |
| POST | `/customer/accounts/new` | Create account (min ₹500 initial deposit) |
| GET | `/customer/accounts/{accountNumber}` | Account detail + full transaction history |
| GET | `/customer/transactions` | Transaction form (deposit / withdraw / transfer) |
| POST | `/customer/transactions/deposit` | Deposit to account |
| POST | `/customer/transactions/withdraw` | Withdraw from account |
| POST | `/customer/transactions/transfer` | Transfer between accounts |
| GET | `/customer/loans` | All customer loans |
| GET | `/customer/loans/apply` | Loan application form |
| POST | `/customer/loans/apply` | Submit loan application |
| GET | `/customer/loans/{loanId}` | Loan detail with EMI, total payable, total interest |
| GET | `/customer/profile` | Customer profile view |

### Manager (`/manager/**`)

| Method | URL | Description |
|---|---|---|
| GET | `/manager/dashboard` | Pending + under-review counts, recent loans |
| GET | `/manager/loans` | All loan applications |
| GET | `/manager/loans/{loanId}` | Loan detail page |
| POST | `/manager/loans/{loanId}/review` | Mark as Under Review |
| POST | `/manager/loans/{loanId}/approve` | Approve loan (requires `remarks` param) |
| POST | `/manager/loans/{loanId}/reject` | Reject loan (requires `remarks` param) |
| POST | `/manager/loans/{loanId}/disburse` | Disburse approved loan |
| GET | `/manager/accounts` | View all accounts |

### Admin (`/admin/**`)

| Method | URL | Description |
|---|---|---|
| GET | `/admin/dashboard` | System totals + recent users and accounts |
| GET | `/admin/users` | All users |
| GET | `/admin/users/new` | Create user form |
| POST | `/admin/users/new` | Create staff/manager/admin user |
| POST | `/admin/users/{id}/deactivate` | Deactivate user |
| POST | `/admin/users/{id}/activate` | Activate user |
| POST | `/admin/users/{id}/unlock` | Unlock locked user |
| GET | `/admin/accounts` | All bank accounts |
| POST | `/admin/accounts/{accountNumber}/freeze` | Freeze account |
| POST | `/admin/accounts/{accountNumber}/activate` | Activate account |
| GET | `/admin/loans` | All loans system-wide |

### Staff (`/staff/**`)

| Method | URL | Description |
|---|---|---|
| GET | `/staff/dashboard` | Totals for accounts, loans, customers |
| GET | `/staff/accounts` | All accounts |
| GET | `/staff/customers` | All customers |

---

## OOP Principles Applied

**Encapsulation** — All model fields are `private`. Business rules live inside entities: `Account.withdraw()` enforces positive amount, ACTIVE status, and sufficient balance. `Loan.approveLoan()` sets the reviewing manager internally. `User.login()` manages `loginAttempts` and `locked` state. No service class directly manipulates field values.

**Inheritance** — `User` is the abstract JPA base class using `SINGLE_TABLE` inheritance. All four subclasses (`Customer`, `BankStaff`, `Manager`, `Admin`) extend it and are stored in a single `users` table with a `user_type` discriminator column. Subclasses add domain-specific fields.

**Polymorphism** — `UserRepository.findAll()` returns `List<User>` containing actual subclass instances. `AccountService` implements the `AccountOperations` interface, enabling controller-level code to depend on the interface contract rather than the concrete class. `TransactionType` enum represents the Deposit/Withdraw/Transfer generalization from the UML class diagram.

**Abstraction** — `AccountOperations` interface abstracts the three core transaction operations. The Thymeleaf `layout.html` and `fragments.html` abstract UI structure. Service classes expose clean domain-language methods (`applyLoan`, `approveLoan`, `disburseLoan`) hiding all JPA complexity.

**Exception Handling** — Domain violations throw `IllegalArgumentException` ("Cannot transfer to same account", "Deposit amount must be positive") and `IllegalStateException` ("Account is not active", "Insufficient balance", "Only approved loans can be disbursed") from within the model layer. Controllers catch `Exception` and surface `error` flash attributes to the UI without exposing stack traces.

---

## Transaction Lifecycle (State Machine)

Every financial operation in `AccountService` is `@Transactional` and follows this exact sequence, mapping directly to the `TransactionStatus` statechart diagram:

```
buildTransaction()    →  status = INITIATED
txn.validate()        →  status = VALIDATING
txn.approve()         →  status = APPROVED
txn.process()         →  status = PROCESSING

account.deposit(amount)   OR   account.withdraw(amount)

txn.setBalanceAfter()     →  balance snapshot stored
txn.complete()            →  status = COMPLETED
accountRepository.save()
transactionRepository.save()

On any exception:
  txn.fail(e.getMessage()) → status = FAILED, failureReason = exception message
  transactionRepository.save()  (always — even on failure, for audit)
  re-throw exception → @Transactional rolls back account balance change
```

For **transfers**, two transactions are built simultaneously (debit TRANSFER + credit DEPOSIT). Both are completed atomically or both are failed — enforced by `@Transactional`.

---

## EMI Calculation

`Loan.calculateEMI()` implements the standard reducing-balance EMI formula:

```
P = loanAmount
r = interestRate / (12 × 100)    ← monthly rate
n = durationMonths

EMI = P × r × (1+r)^n  /  ((1+r)^n − 1)

getTotalPayable()  = EMI × n
getTotalInterest() = getTotalPayable() − P
```

If `interestRate == 0`, EMI = P / n (simple equal installments). All results are rounded to 2 decimal places using `RoundingMode.HALF_UP`.

---

## Future Enhancements

- **Persistent Database** — Replace H2 in-memory with MySQL or PostgreSQL. Change `spring.jpa.hibernate.ddl-auto` to `validate` and add a MySQL datasource in `application.properties`. All entities are JPA-compliant and require no code changes.

- **Account Lock on Login Failures** — `User.login()` already implements the locking logic (`loginAttempts >= 3 → locked = true`), but Spring Security's authentication flow doesn't call this method directly. An `AuthenticationFailureEventListener` can wire them together.

- **Loan Repayment Tracking** — The `LoanStatus.CLOSED` state exists but is never triggered. Adding an EMI repayment schedule and a repayment entity would complete the loan lifecycle per the statechart.

- **Email Notifications** — The OOAD sequence diagrams model customer notification on loan status changes. Spring Mail + `JavaMailSender` can be wired into `LoanService.approveLoan()` and `rejectLoan()`.

- **Pagination** — `findAll()` in `AccountService` and `LoanService` returns all records. Spring Data's `Pageable` and `Page<T>` can be added to repository methods for large datasets.

- **REST API Layer** — Adding `@RestController` endpoints alongside existing MVC controllers exposes the same service layer as a JSON API for mobile or SPA clients.

- **FIXED_DEPOSIT and RECURRING_DEPOSIT Logic** — `AccountType` already includes `FIXED_DEPOSIT` and `RECURRING_DEPOSIT` enum values but no business rules differentiate them from SAVINGS/CURRENT yet. Maturity date, lock-in period, and interest credit logic would complete these types.

---

## Challenges Faced

**1. Role-Based Dashboard Redirect**
Spring Security's `defaultSuccessUrl("/dashboard", true)` sends all roles to the same URL. `AuthController.dashboard()` reads the first `GrantedAuthority` string from the `Authentication` object and uses a Java `switch` expression to redirect each role to its specific dashboard route.

**2. Transaction Atomicity for Transfers**
A fund transfer requires two balance changes on two different accounts. Wrapping the entire `AccountService.transfer()` method in `@Transactional` ensures that if the credit deposit fails after the debit withdraw succeeds, Hibernate rolls back both balance changes. Both transaction records are always saved to the `transactions` table (even FAILED ones) for a complete audit trail.

**3. Manager Identity in Loan Approval**
`ManagerController.approveLoan()` receives only the loan ID and remarks as form parameters. The manager's identity must come from the authenticated session, not from a form field (which could be spoofed). This is resolved by reading `auth.getName()` (the email from the Spring Security session) and calling `userService.getManagerByEmail()` to retrieve the `Manager` entity.

**4. Customer Ownership Enforcement**
Without an ownership check, any authenticated customer could access `/customer/accounts/{anyAccountNumber}` and see another customer's data. `CustomerController.accountDetail()` fetches the account and compares `account.getCustomer().getUserId()` with the logged-in customer's ID before rendering, silently redirecting on mismatch.

**5. DataInitializer Idempotency**
`DataInitializer.run()` checks `userRepository.count() > 0` before seeding. This prevents duplicate records on restart if `ddl-auto` is ever switched from `create-drop` to `update` for persistence.

---

## Contributors

| Name | Registration Number | Course |
|---|---|---|
| Dhayanidhi S | 23BIT0214 | BITE404E — Object Oriented Analysis and Design Lab, VIT University |

---

## License

This project is licensed under the MIT License.

```
MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## Acknowledgements

- **VIT University** — BITE404E Object Oriented Analysis and Design Lab
- [Spring Boot](https://spring.io/projects/spring-boot) — application framework
- [Spring Security](https://spring.io/projects/spring-security) — authentication and authorization
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) — ORM and repository abstraction
- [Thymeleaf](https://www.thymeleaf.org/) — server-side template engine
- [H2 Database](https://www.h2database.com/) — in-memory database for development
- **UML 2.5 Specification** — Grady Booch, James Rumbaugh, Ivar Jacobson (Three Amigos / RUP methodology)
