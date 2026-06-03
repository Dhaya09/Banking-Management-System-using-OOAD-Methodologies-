package com.bms.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransactionDto {
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum amount is ₹1")
    private BigDecimal amount;

    private String targetAccountNumber;  // for transfer

    private String description;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public void setTargetAccountNumber(String targetAccountNumber) { this.targetAccountNumber = targetAccountNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
