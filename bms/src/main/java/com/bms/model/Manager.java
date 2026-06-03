package com.bms.model;

import com.bms.enums.Role;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UML: Generalization — Manager extends User
 * Business Rule: Only Manager can approve/reject loans
 */
@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends User {

    private String employeeId;
    private String branchCode;

    /** UML: Association — Manager reviews Loans */
    @OneToMany(mappedBy = "reviewedBy", fetch = FetchType.LAZY)
    private List<Loan> reviewedLoans = new ArrayList<>();

    public Manager() {
        setRole(Role.MANAGER);
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public List<Loan> getReviewedLoans() { return reviewedLoans; }
    public void setReviewedLoans(List<Loan> reviewedLoans) { this.reviewedLoans = reviewedLoans; }
}
