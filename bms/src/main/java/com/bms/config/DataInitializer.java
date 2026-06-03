package com.bms.config;

import com.bms.enums.AccountStatus;
import com.bms.enums.AccountType;
import com.bms.model.*;
import com.bms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds default users for demonstration / viva
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) return;  // already seeded

        // ── Admin ───────────────────────────────────────────────
        Admin admin = new Admin();
        admin.setName("Super Admin");
        admin.setEmail("admin@bms.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("9000000001");
        admin.setAdminLevel("SUPER");
        admin.setDepartment("IT");
        admin.setActive(true);
        userRepository.save(admin);

        // ── Manager ─────────────────────────────────────────────
        Manager manager = new Manager();
        manager.setName("Ramesh Kumar");
        manager.setEmail("manager@bms.com");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setPhone("9000000002");
        manager.setEmployeeId("EMP001");
        manager.setBranchCode("BMS-CHN-01");
        manager.setActive(true);
        managerRepository.save(manager);

        // ── Bank Staff ──────────────────────────────────────────
        BankStaff staff = new BankStaff();
        staff.setName("Priya Sharma");
        staff.setEmail("staff@bms.com");
        staff.setPassword(passwordEncoder.encode("staff123"));
        staff.setPhone("9000000003");
        staff.setEmployeeId("EMP002");
        staff.setDepartment("Operations");
        staff.setDesignation("Senior Clerk");
        staff.setActive(true);
        userRepository.save(staff);

        // ── Customer ────────────────────────────────────────────
        Customer customer = new Customer();
        customer.setName("Arjun Patel");
        customer.setEmail("customer@bms.com");
        customer.setPassword(passwordEncoder.encode("customer123"));
        customer.setPhone("9000000004");
        customer.setAddress("123 MG Road, Chennai, TN");
        customer.setCustomerCode("CUST260101");
        customer.setDateOfBirth("1990-05-15");
        customer.setNationality("Indian");
        customer.setActive(true);
        customerRepository.save(customer);

        // ── Sample Account for customer ─────────────────────────
        Account savings = new Account();
        savings.setAccountNumber("BMS260101001");
        savings.setAccountType(AccountType.SAVINGS);
        savings.setBalance(new BigDecimal("25000.00"));
        savings.setStatus(AccountStatus.ACTIVE);
        savings.setCustomer(customer);
        savings.setCreatedDate(LocalDateTime.now());
        accountRepository.save(savings);

        Account current = new Account();
        current.setAccountNumber("BMS260101002");
        current.setAccountType(AccountType.CURRENT);
        current.setBalance(new BigDecimal("75000.00"));
        current.setStatus(AccountStatus.ACTIVE);
        current.setCustomer(customer);
        current.setCreatedDate(LocalDateTime.now());
        accountRepository.save(current);

        System.out.println("========================================");
        System.out.println("  BMS Default Credentials:");
        System.out.println("  Admin    : admin@bms.com / admin123");
        System.out.println("  Manager  : manager@bms.com / manager123");
        System.out.println("  Staff    : staff@bms.com / staff123");
        System.out.println("  Customer : customer@bms.com / customer123");
        System.out.println("  URL      : http://localhost:8080");
        System.out.println("========================================");
    }
}
