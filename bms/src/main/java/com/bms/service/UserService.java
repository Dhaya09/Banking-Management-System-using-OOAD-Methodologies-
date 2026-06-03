package com.bms.service;

import com.bms.dto.RegistrationDto;
import com.bms.dto.UserCreationDto;
import com.bms.enums.Role;
import com.bms.model.*;
import com.bms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * UML: AuthenticationController + AdminController (Business Logic Layer)
 * Handles login, registration, role management
 */
@Service
@Transactional
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /** UML: Customer Registration */
    public Customer registerCustomer(RegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already registered: " + dto.getEmail());

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNationality(dto.getNationality());
        customer.setCustomerCode(generateCustomerCode());
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    /** UML: Admin creates any user (BankStaff / Manager / Admin) */
    public User createUser(UserCreationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already registered");

        User user;
        switch (dto.getRole()) {
            case BANK_STAFF -> {
                BankStaff staff = new BankStaff();
                staff.setEmployeeId(dto.getEmployeeId());
                staff.setDepartment(dto.getDepartment());
                user = staff;
            }
            case MANAGER -> {
                Manager manager = new Manager();
                manager.setEmployeeId(dto.getEmployeeId());
                manager.setBranchCode(dto.getBranchCode());
                user = manager;
            }
            case ADMIN -> {
                Admin admin = new Admin();
                admin.setDepartment(dto.getDepartment());
                user = admin;
            }
            default -> throw new IllegalArgumentException("Use registerCustomer() for customers");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setActive(true);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }

    public void unlockUser(Long id) {
        User user = getUserById(id);
        user.setLocked(false);
        user.setLoginAttempts(0);
        userRepository.save(user);
    }

    public void changeRole(Long id, Role newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        userRepository.save(user);
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Manager getManagerByEmail(String email) {
        return managerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String generateCustomerCode() {
        return "CUST" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + String.format("%04d", new Random().nextInt(9999));
    }
}
