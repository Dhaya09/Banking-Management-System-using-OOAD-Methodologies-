package com.bms.service;

import com.bms.dto.AccountCreationDto;
import com.bms.enums.AccountStatus;
import com.bms.enums.TransactionStatus;
import com.bms.enums.TransactionType;
import com.bms.model.*;
import com.bms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * UML: AccountController (Business Logic Layer)
 * Implements AccountOperations (Realization)
 * Depends on Account, Transaction, Customer entities (Dependency)
 */
@Service
@Transactional
public class AccountService implements AccountOperations {

    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private CustomerRepository customerRepository;

    // ── Account Management ──────────────────────────────────────────────────

    public Account createAccount(Customer customer, AccountCreationDto dto) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(dto.getAccountType());
        account.setCustomer(customer);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        account = accountRepository.save(account);

        // Initial deposit transaction
        if (dto.getInitialDeposit() != null && dto.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            deposit(account.getAccountNumber(), dto.getInitialDeposit(), "Initial deposit");
        }
        return account;
    }

    public List<Account> getCustomerAccounts(Customer customer) {
        return accountRepository.findByCustomerOrderByCreatedDateDesc(customer);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    public void freezeAccount(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        account.freezeAccount();
        accountRepository.save(account);
    }

    public void closeAccount(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        account.closeAccount();
        accountRepository.save(account);
    }

    public void activateAccount(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        account.activate();
        accountRepository.save(account);
    }

    // ── Transaction Operations ──────────────────────────────────────────────

    @Override
    public void deposit(String accountNumber, BigDecimal amount, String description) {
        Account account = getAccountByNumber(accountNumber);

        Transaction txn = buildTransaction(account, TransactionType.DEPOSIT, amount, description);
        txn.validate();
        txn.approve();
        txn.process();

        account.deposit(amount);
        txn.setBalanceAfter(account.getBalance());
        txn.complete();

        accountRepository.save(account);
        transactionRepository.save(txn);
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount, String description) {
        Account account = getAccountByNumber(accountNumber);

        Transaction txn = buildTransaction(account, TransactionType.WITHDRAW, amount, description);
        txn.validate();
        txn.approve();
        txn.process();

        try {
            account.withdraw(amount);
            txn.setBalanceAfter(account.getBalance());
            txn.complete();
            accountRepository.save(account);
        } catch (Exception e) {
            txn.fail(e.getMessage());
            throw e;
        } finally {
            transactionRepository.save(txn);
        }
    }

    /**
     * UML: Transfer — atomic debit/credit (simulated parallel, enforced by @Transactional)
     * Sequence: Validate balance → Debit source → Credit target → Complete
     */
    @Override
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        Account fromAccount = getAccountByNumber(fromAccountNumber);
        Account toAccount = getAccountByNumber(toAccountNumber);

        if (fromAccountNumber.equals(toAccountNumber))
            throw new IllegalArgumentException("Cannot transfer to same account");

        // Debit transaction
        Transaction debitTxn = buildTransaction(fromAccount, TransactionType.TRANSFER, amount,
                "Transfer to " + toAccountNumber + (description != null ? " - " + description : ""));
        debitTxn.setTargetAccountNumber(toAccountNumber);
        debitTxn.validate();
        debitTxn.approve();
        debitTxn.process();

        // Credit transaction
        Transaction creditTxn = buildTransaction(toAccount, TransactionType.DEPOSIT, amount,
                "Transfer from " + fromAccountNumber + (description != null ? " - " + description : ""));
        creditTxn.validate();
        creditTxn.approve();
        creditTxn.process();

        try {
            fromAccount.transfer(amount);         // atomic debit
            toAccount.deposit(amount);            // atomic credit

            debitTxn.setBalanceAfter(fromAccount.getBalance());
            creditTxn.setBalanceAfter(toAccount.getBalance());
            debitTxn.complete();
            creditTxn.complete();

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
        } catch (Exception e) {
            debitTxn.fail(e.getMessage());
            creditTxn.fail(e.getMessage());
            throw e;
        } finally {
            transactionRepository.save(debitTxn);
            transactionRepository.save(creditTxn);
        }
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        return transactionRepository.findByAccountOrderByTransactionDateDesc(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Transaction buildTransaction(Account account, TransactionType type, BigDecimal amount, String description) {
        Transaction txn = new Transaction();
        txn.setAccount(account);
        txn.setTransactionType(type);
        txn.setAmount(amount);
        txn.setDescription(description);
        txn.setTransactionReference(generateTxnRef());
        txn.setStatus(TransactionStatus.INITIATED);
        txn.setTransactionDate(LocalDateTime.now());
        return txn;
    }

    private String generateAccountNumber() {
        String prefix = "BMS";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = String.format("%04d", new Random().nextInt(9999));
        String candidate = prefix + timestamp + random;
        while (accountRepository.existsByAccountNumber(candidate)) {
            random = String.format("%04d", new Random().nextInt(9999));
            candidate = prefix + timestamp + random;
        }
        return candidate;
    }

    private String generateTxnRef() {
        return "TXN" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(999));
    }
}
