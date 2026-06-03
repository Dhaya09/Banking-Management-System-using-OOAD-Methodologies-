package com.bms.service;

/**
 * UML: Realization — Interface implemented by Service/Controllers
 * Dependency Inversion Principle
 */
public interface AccountOperations {
    void deposit(String accountNumber, java.math.BigDecimal amount, String description);
    void withdraw(String accountNumber, java.math.BigDecimal amount, String description);
    void transfer(String fromAccount, String toAccount, java.math.BigDecimal amount, String description);
}
