package com.bms.model;

import com.bms.enums.AccountStatus;
import com.bms.enums.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UML: Account Class
 * Attributes: accountNumber, accountType, balance, status, createdDate
 * Methods: deposit(), withdraw(), transfer(), checkBalance(), freezeAccount(), closeAccount()
 *
 * UML: Aggregation — Bank aggregates Accounts
 * UML: Composition — Account ↔ Transactions (strong ownership)
 * UML: Association — Customer ↔ Account
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.CREATED;

    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime lastTransactionDate;

    /** UML: Association — many accounts belong to one customer */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** UML: Composition — Account owns Transactions */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("transactionDate DESC")
    private List<Transaction> transactions = new ArrayList<>();

    // ── Business Methods ────────────────────────────────────────────────────

    /** UML: deposit() */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account is not active");
        this.balance = this.balance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }

    /** UML: withdraw() */
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account is not active");
        if (this.balance.compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient balance");
        this.balance = this.balance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
        if (this.balance.compareTo(BigDecimal.ZERO) < 0)
            this.status = AccountStatus.OVERDRAWN;
    }

    /** UML: transfer() — debit side; credit happens on target account */
    public void transfer(BigDecimal amount) {
        withdraw(amount);
    }

    /** UML: checkBalance() */
    public BigDecimal checkBalance() {
        return this.balance;
    }

    /** UML: freezeAccount() */
    public void freezeAccount() {
        this.status = AccountStatus.FROZEN;
    }

    /** UML: closeAccount() */
    public void closeAccount() {
        this.status = AccountStatus.CLOSED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}
