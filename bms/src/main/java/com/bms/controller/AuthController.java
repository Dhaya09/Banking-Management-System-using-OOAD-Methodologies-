package com.bms.controller;

import com.bms.dto.RegistrationDto;
import com.bms.model.User;
import com.bms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UML: AuthenticationController (Presentation Layer)
 * login(), logout(), register()
 */
@Controller
public class AuthController {

    @Autowired private UserService userService;

    @GetMapping("/")
    public String index(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) return "redirect:/dashboard";
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationDto registrationDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) return "auth/register";

        try {
            userService.registerCustomer(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    /** Smart dashboard redirect based on role */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN"      -> "redirect:/admin/dashboard";
            case "ROLE_MANAGER"    -> "redirect:/manager/dashboard";
            case "ROLE_BANK_STAFF" -> "redirect:/staff/dashboard";
            case "ROLE_CUSTOMER"   -> "redirect:/customer/dashboard";
            default -> "redirect:/login";
        };
    }
}
