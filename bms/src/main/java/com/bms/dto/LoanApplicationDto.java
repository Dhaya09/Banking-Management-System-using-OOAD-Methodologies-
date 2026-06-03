package com.bms.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LoanApplicationDto {
    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is ₹1,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "30.0")
    private BigDecimal interestRate;

    @NotNull(message = "Duration is required")
    @Min(value = 1) @Max(value = 360)
    private Integer durationMonths;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}
