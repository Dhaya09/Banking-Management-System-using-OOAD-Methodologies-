package com.bms.dto;

import com.bms.enums.AccountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AccountCreationDto {
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "500.00", message = "Minimum initial deposit is ₹500")
    private BigDecimal initialDeposit;

    private String description;

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
