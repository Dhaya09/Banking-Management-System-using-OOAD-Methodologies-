package com.bms.model;

import com.bms.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * UML: Abstract Class — User
 * Generalization Root for Customer, BankStaff, Manager, Admin
 *
 * Attributes: userId, name, email, password, role, status
 * Methods: login(), logout(), updateProfile(), changePassword()
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    private int loginAttempts = 0;

    private boolean locked = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLogin;

    private String phone;

    private String address;

    // ── Abstract behaviour ──────────────────────────────────────────────────

    /** UML: login() — validates credentials, enforces lock after 3 failures */
    public boolean login(String rawPassword) {
        if (locked) return false;
        if (this.password.equals(rawPassword)) {
            this.loginAttempts = 0;
            this.lastLogin = LocalDateTime.now();
            return true;
        }
        this.loginAttempts++;
        if (this.loginAttempts >= 3) this.locked = true;
        return false;
    }

    /** UML: logout() */
    public void logout() {
        this.lastLogin = LocalDateTime.now();
    }

    /** UML: updateProfile() */
    public void updateProfile(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    /** UML: changePassword() */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(int loginAttempts) { this.loginAttempts = loginAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
