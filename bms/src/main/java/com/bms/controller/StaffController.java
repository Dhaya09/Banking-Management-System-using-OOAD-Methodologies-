package com.bms.controller;

import com.bms.service.AccountService;
import com.bms.service.LoanService;
import com.bms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired private UserService userService;
    @Autowired private AccountService accountService;
    @Autowired private LoanService loanService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalAccounts", accountService.getAllAccounts().size());
        model.addAttribute("totalLoans", loanService.getAllLoans().size());
        model.addAttribute("totalCustomers", userService.getUsersByRole(com.bms.enums.Role.CUSTOMER).size());
        model.addAttribute("recentAccounts", accountService.getAllAccounts().stream().limit(8).toList());
        return "staff/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "staff/accounts";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", userService.getUsersByRole(com.bms.enums.Role.CUSTOMER));
        return "staff/customers";
    }
}
