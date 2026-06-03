package com.bms.model;

import com.bms.enums.Role;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UML: Generalization — Customer extends User
 * Association: Customer ↔ Account (1..*)
 */
@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    private String customerCode;

    private String dateOfBirth;

    private String nationality;

    /** UML: Composition — Account → Transactions; Association Customer ↔ Account */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    /** UML: Association — Customer applies for Loans */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    public Customer() {
        setRole(Role.CUSTOMER);
    }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }
}
