package com.bms.model;

import com.bms.enums.TransactionStatus;
import com.bms.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UML: Transaction — Parent class
 * Child types: Deposit, Withdraw, Transfer — realized via TransactionType enum
 *
 * States: Initiated → Validating → Approved → Processing → Completed/Failed
 * UML: Composition — Account owns Transaction (part-of relationship)
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(unique = true)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;  // DEPOSIT / WITHDRAW / TRANSFER

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.INITIATED;

    private LocalDateTime transactionDate = LocalDateTime.now();

    private String description;

    private String failureReason;

    /** UML: Composition — belongs to source Account */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** For TRANSFER: target account number */
    private String targetAccountNumber;

    // ── State Machine Methods ───────────────────────────────────────────────

    public void initiate() { this.status = TransactionStatus.INITIATED; }
    public void validate() { this.status = TransactionStatus.VALIDATING; }
    public void approve() { this.status = TransactionStatus.APPROVED; }
    public void process() { this.status = TransactionStatus.PROCESSING; }
    public void complete() { this.status = TransactionStatus.COMPLETED; }
    public void fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getTargetAccountNumber() { return targetAccountNumber; }
    public void setTargetAccountNumber(String targetAccountNumber) { this.targetAccountNumber = targetAccountNumber; }
}
