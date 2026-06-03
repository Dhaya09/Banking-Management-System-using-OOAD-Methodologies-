package com.bms.controller;

import com.bms.dto.AccountCreationDto;
import com.bms.dto.LoanApplicationDto;
import com.bms.dto.TransactionDto;
import com.bms.enums.AccountType;
import com.bms.model.*;
import com.bms.service.AccountService;
import com.bms.service.LoanService;
import com.bms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * UML: AccountController + TransactionController + LoanController
 * Presentation Layer for Customer role
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired private UserService userService;
    @Autowired private AccountService accountService;
    @Autowired private LoanService loanService;

    // ── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        List<Account> accounts = accountService.getCustomerAccounts(customer);
        List<Loan> loans = loanService.getCustomerLoans(customer);

        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);
        model.addAttribute("loans", loans);
        model.addAttribute("totalBalance", accounts.stream()
                .filter(a -> a.getStatus().name().equals("ACTIVE"))
                .mapToDouble(a -> a.getBalance().doubleValue()).sum());
        return "customer/dashboard";
    }

    // ── Account ─────────────────────────────────────────────────────────────

    @GetMapping("/accounts")
    public String accounts(Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        model.addAttribute("accounts", accountService.getCustomerAccounts(customer));
        model.addAttribute("customer", customer);
        return "customer/accounts";
    }

    @GetMapping("/accounts/new")
    public String newAccountForm(Model model) {
        model.addAttribute("accountCreationDto", new AccountCreationDto());
        model.addAttribute("accountTypes", AccountType.values());
        return "customer/account-create";
    }

    @PostMapping("/accounts/new")
    public String createAccount(@Valid @ModelAttribute AccountCreationDto accountCreationDto,
                                BindingResult result,
                                Authentication auth,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return "customer/account-create";
        }
        try {
            Customer customer = getCustomer(auth);
            Account account = accountService.createAccount(customer, accountCreationDto);
            redirectAttributes.addFlashAttribute("success",
                    "Account created successfully! Account No: " + account.getAccountNumber());
            return "redirect:/customer/accounts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("accountTypes", AccountType.values());
            return "customer/account-create";
        }
    }

    @GetMapping("/accounts/{accountNumber}")
    public String accountDetail(@PathVariable String accountNumber, Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        Account account = accountService.getAccountByNumber(accountNumber);

        // Security: ensure account belongs to this customer
        if (!account.getCustomer().getUserId().equals(customer.getUserId()))
            return "redirect:/customer/accounts";

        model.addAttribute("account", account);
        model.addAttribute("transactions", accountService.getTransactionHistory(accountNumber));
        return "customer/account-detail";
    }

    // ── Transactions ─────────────────────────────────────────────────────────

    @GetMapping("/transactions")
    public String transactionPage(Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        model.addAttribute("accounts", accountService.getCustomerAccounts(customer));
        model.addAttribute("transactionDto", new TransactionDto());
        return "customer/transactions";
    }

    @PostMapping("/transactions/deposit")
    public String deposit(@Valid @ModelAttribute TransactionDto dto, BindingResult result,
                          Authentication auth, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            Customer c = getCustomer(auth);
            model.addAttribute("accounts", accountService.getCustomerAccounts(c));
            return "customer/transactions";
        }
        try {
            accountService.deposit(dto.getAccountNumber(), dto.getAmount(), dto.getDescription());
            ra.addFlashAttribute("success", "Deposit of ₹" + dto.getAmount() + " successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/transactions";
    }

    @PostMapping("/transactions/withdraw")
    public String withdraw(@Valid @ModelAttribute TransactionDto dto, BindingResult result,
                           Authentication auth, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            Customer c = getCustomer(auth);
            model.addAttribute("accounts", accountService.getCustomerAccounts(c));
            return "customer/transactions";
        }
        try {
            accountService.withdraw(dto.getAccountNumber(), dto.getAmount(), dto.getDescription());
            ra.addFlashAttribute("success", "Withdrawal of ₹" + dto.getAmount() + " successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/transactions";
    }

    @PostMapping("/transactions/transfer")
    public String transfer(@Valid @ModelAttribute TransactionDto dto, BindingResult result,
                           Authentication auth, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            Customer c = getCustomer(auth);
            model.addAttribute("accounts", accountService.getCustomerAccounts(c));
            return "customer/transactions";
        }
        try {
            accountService.transfer(dto.getAccountNumber(), dto.getTargetAccountNumber(),
                    dto.getAmount(), dto.getDescription());
            ra.addFlashAttribute("success", "Transfer of ₹" + dto.getAmount() + " successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/transactions";
    }

    // ── Loans ────────────────────────────────────────────────────────────────

    @GetMapping("/loans")
    public String loansPage(Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        model.addAttribute("loans", loanService.getCustomerLoans(customer));
        model.addAttribute("customer", customer);
        return "customer/loans";
    }

    @GetMapping("/loans/apply")
    public String applyLoanForm(Model model) {
        model.addAttribute("loanApplicationDto", new LoanApplicationDto());
        return "customer/loan-apply";
    }

    @PostMapping("/loans/apply")
    public String applyLoan(@Valid @ModelAttribute LoanApplicationDto dto, BindingResult result,
                            Authentication auth, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) return "customer/loan-apply";
        try {
            Customer customer = getCustomer(auth);
            Loan loan = loanService.applyLoan(customer, dto);
            ra.addFlashAttribute("success", "Loan application submitted! Ref: " + loan.getLoanReference());
            return "redirect:/customer/loans";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "customer/loan-apply";
        }
    }

    @GetMapping("/loans/{loanId}")
    public String loanDetail(@PathVariable Long loanId, Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        Loan loan = loanService.getById(loanId);
        if (!loan.getCustomer().getUserId().equals(customer.getUserId()))
            return "redirect:/customer/loans";
        model.addAttribute("loan", loan);
        return "customer/loan-detail";
    }

    // ── Profile ──────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", getCustomer(auth));
        return "customer/profile";
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Customer getCustomer(Authentication auth) {
        return userService.getCustomerByEmail(auth.getName());
    }
}
