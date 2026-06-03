package com.bms.repository;

import com.bms.enums.LoanStatus;
import com.bms.model.Customer;
import com.bms.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomer(Customer customer);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByCustomerOrderByAppliedDateDesc(Customer customer);
}
