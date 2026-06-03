package com.bms.model;

import com.bms.enums.LoanStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * UML: Loan Class
 * Attributes: loanId, loanAmount, interestRate, durationMonths, status
 * Methods: applyLoan(), approveLoan(), rejectLoan(), calculateEMI()
 *
 * States: Draft → Submitted → UnderReview → Approved/Rejected → Disbursed → Closed
 * Business Rule: Only Manager can approve/reject
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Column(unique = true)
    private String loanReference;

    private String loanType;  // HOME, CAR, PERSONAL, EDUCATION

    @Column(precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.DRAFT;

    private String purpose;

    private String remarks;

    private LocalDateTime appliedDate = LocalDateTime.now();

    private LocalDateTime reviewedDate;

    private LocalDateTime disbursedDate;

    /** UML: Association — Customer applies for Loan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** UML: Association — Manager reviews Loan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Manager reviewedBy;

    // ── Business Methods ────────────────────────────────────────────────────

    /** UML: applyLoan() */
    public void applyLoan() {
        if (this.status == LoanStatus.DRAFT) {
            this.status = LoanStatus.SUBMITTED;
            this.appliedDate = LocalDateTime.now();
        }
    }

    /** UML: approveLoan() — Business Rule: Only Manager */
    public void approveLoan(Manager manager, String remarks) {
        this.status = LoanStatus.APPROVED;
        this.reviewedBy = manager;
        this.remarks = remarks;
        this.reviewedDate = LocalDateTime.now();
    }

    /** UML: rejectLoan() — Business Rule: Only Manager */
    public void rejectLoan(Manager manager, String remarks) {
        this.status = LoanStatus.REJECTED;
        this.reviewedBy = manager;
        this.remarks = remarks;
        this.reviewedDate = LocalDateTime.now();
    }

    public void disburse() {
        this.status = LoanStatus.DISBURSED;
        this.disbursedDate = LocalDateTime.now();
    }

    /**
     * UML: calculateEMI()
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     * where P = principal, r = monthly rate, n = months
     */
    public BigDecimal calculateEMI() {
        if (loanAmount == null || interestRate == null || durationMonths == null)
            return BigDecimal.ZERO;

        double P = loanAmount.doubleValue();
        double annualRate = interestRate.doubleValue();
        double r = annualRate / (12 * 100);  // monthly interest rate
        int n = durationMonths;

        if (r == 0) return loanAmount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        double emi = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPayable() {
        return calculateEMI().multiply(BigDecimal.valueOf(durationMonths != null ? durationMonths : 0));
    }

    public BigDecimal getTotalInterest() {
        return getTotalPayable().subtract(loanAmount != null ? loanAmount : BigDecimal.ZERO);
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }

    public String getLoanReference() { return loanReference; }
    public void setLoanReference(String loanReference) { this.loanReference = loanReference; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }

    public LocalDateTime getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDateTime reviewedDate) { this.reviewedDate = reviewedDate; }

    public LocalDateTime getDisbursedDate() { return disbursedDate; }
    public void setDisbursedDate(LocalDateTime disbursedDate) { this.disbursedDate = disbursedDate; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Manager getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Manager reviewedBy) { this.reviewedBy = reviewedBy; }
}
