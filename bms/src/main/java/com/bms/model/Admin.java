package com.bms.model;

import com.bms.enums.Role;
import jakarta.persistence.*;

/**
 * UML: Generalization — Admin extends User
 * Manages users, assigns roles, deactivates users
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    private String adminLevel;
    private String department;

    public Admin() {
        setRole(Role.ADMIN);
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
