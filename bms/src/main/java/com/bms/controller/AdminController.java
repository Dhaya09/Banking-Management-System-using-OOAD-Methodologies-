package com.bms.controller;

import com.bms.dto.UserCreationDto;
import com.bms.enums.Role;
import com.bms.model.*;
import com.bms.service.AccountService;
import com.bms.service.LoanService;
import com.bms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UML: AdminController (Presentation Layer)
 * Manage users, assign roles, deactivate users
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private AccountService accountService;
    @Autowired private LoanService loanService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",  userService.getAllUsers().size());
        model.addAttribute("totalAccounts", accountService.getAllAccounts().size());
        model.addAttribute("totalLoans", loanService.getAllLoans().size());
        model.addAttribute("recentUsers", userService.getAllUsers().stream().limit(5).toList());
        model.addAttribute("recentAccounts", accountService.getAllAccounts().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String allUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("userCreationDto", new UserCreationDto());
        model.addAttribute("roles", new Role[]{Role.BANK_STAFF, Role.MANAGER, Role.ADMIN});
        return "admin/user-create";
    }

    @PostMapping("/users/new")
    public String createUser(@Valid @ModelAttribute UserCreationDto dto, BindingResult result,
                             RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.BANK_STAFF, Role.MANAGER, Role.ADMIN});
            return "admin/user-create";
        }
        try {
            userService.createUser(dto);
            ra.addFlashAttribute("success", "User created successfully: " + dto.getEmail());
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.BANK_STAFF, Role.MANAGER, Role.ADMIN});
            return "admin/user-create";
        }
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        userService.deactivateUser(id);
        ra.addFlashAttribute("success", "User deactivated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        userService.activateUser(id);
        ra.addFlashAttribute("success", "User activated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlock(@PathVariable Long id, RedirectAttributes ra) {
        userService.unlockUser(id);
        ra.addFlashAttribute("success", "User unlocked.");
        return "redirect:/admin/users";
    }

    @GetMapping("/accounts")
    public String allAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "admin/accounts";
    }

    @PostMapping("/accounts/{accountNumber}/freeze")
    public String freeze(@PathVariable String accountNumber, RedirectAttributes ra) {
        accountService.freezeAccount(accountNumber);
        ra.addFlashAttribute("success", "Account frozen.");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/{accountNumber}/activate")
    public String activateAccount(@PathVariable String accountNumber, RedirectAttributes ra) {
        accountService.activateAccount(accountNumber);
        ra.addFlashAttribute("success", "Account activated.");
        return "redirect:/admin/accounts";
    }

    @GetMapping("/loans")
    public String allLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        return "admin/loans";
    }
}
