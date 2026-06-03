package com.bms.model;

import com.bms.enums.Role;
import jakarta.persistence.*;

/**
 * UML: Generalization — BankStaff extends User
 */
@Entity
@DiscriminatorValue("BANK_STAFF")
public class BankStaff extends User {

    private String employeeId;
    private String department;
    private String designation;

    public BankStaff() {
        setRole(Role.BANK_STAFF);
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
