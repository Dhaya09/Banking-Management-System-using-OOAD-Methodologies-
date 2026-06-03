package com.bms.controller;

import com.bms.model.*;
import com.bms.service.AccountService;
import com.bms.service.LoanService;
import com.bms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * UML: Manager role — approve/reject loans, view accounts
 * Business Rule: ONLY Manager can approve/reject loans
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired private UserService userService;
    @Autowired private LoanService loanService;
    @Autowired private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        List<Loan> pending = loanService.getPendingLoans();
        List<Loan> underReview = loanService.getUnderReviewLoans();
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("underReviewCount", underReview.size());
        model.addAttribute("pendingLoans", pending.stream().limit(5).toList());
        model.addAttribute("allLoans", loanService.getAllLoans().stream().limit(10).toList());
        return "manager/dashboard";
    }

    @GetMapping("/loans")
    public String allLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        return "manager/loans";
    }

    @GetMapping("/loans/{loanId}")
    public String loanDetail(@PathVariable Long loanId, Model model) {
        Loan loan = loanService.getById(loanId);
        model.addAttribute("loan", loan);
        return "manager/loan-detail";
    }

    @PostMapping("/loans/{loanId}/review")
    public String markUnderReview(@PathVariable Long loanId, RedirectAttributes ra) {
        loanService.markUnderReview(loanId);
        ra.addFlashAttribute("success", "Loan marked as Under Review.");
        return "redirect:/manager/loans";
    }

    @PostMapping("/loans/{loanId}/approve")
    public String approveLoan(@PathVariable Long loanId,
                              @RequestParam String remarks,
                              Authentication auth,
                              RedirectAttributes ra) {
        Manager manager = userService.getManagerByEmail(auth.getName());
        loanService.approveLoan(loanId, manager.getUserId(), remarks);
        ra.addFlashAttribute("success", "Loan APPROVED successfully.");
        return "redirect:/manager/loans";
    }

    @PostMapping("/loans/{loanId}/reject")
    public String rejectLoan(@PathVariable Long loanId,
                             @RequestParam String remarks,
                             Authentication auth,
                             RedirectAttributes ra) {
        Manager manager = userService.getManagerByEmail(auth.getName());
        loanService.rejectLoan(loanId, manager.getUserId(), remarks);
        ra.addFlashAttribute("success", "Loan REJECTED.");
        return "redirect:/manager/loans";
    }

    @PostMapping("/loans/{loanId}/disburse")
    public String disburseLoan(@PathVariable Long loanId, RedirectAttributes ra) {
        loanService.disburseLoan(loanId);
        ra.addFlashAttribute("success", "Loan disbursed successfully.");
        return "redirect:/manager/loans";
    }

    @GetMapping("/accounts")
    public String viewAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "manager/accounts";
    }
}
