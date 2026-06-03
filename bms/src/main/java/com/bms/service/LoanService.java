package com.bms.service;

import com.bms.dto.LoanApplicationDto;
import com.bms.enums.LoanStatus;
import com.bms.model.*;
import com.bms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * UML: LoanController (Business Logic Layer)
 * Loan lifecycle: Draft → Submitted → UnderReview → Approved/Rejected → Disbursed → Closed
 * Business Rule: Only Manager can approve/reject
 */
@Service
@Transactional
public class LoanService {

    @Autowired private LoanRepository loanRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ManagerRepository managerRepository;

    /** UML: applyLoan() — Customer initiates */
    public Loan applyLoan(Customer customer, LoanApplicationDto dto) {
        Loan loan = new Loan();
        loan.setLoanReference(generateLoanRef());
        loan.setLoanType(dto.getLoanType());
        loan.setLoanAmount(dto.getLoanAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setDurationMonths(dto.getDurationMonths());
        loan.setPurpose(dto.getPurpose());
        loan.setCustomer(customer);
        loan.setStatus(LoanStatus.SUBMITTED);
        loan.setAppliedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    /** Move to Under Review */
    public void markUnderReview(Long loanId) {
        Loan loan = getById(loanId);
        loan.setStatus(LoanStatus.UNDER_REVIEW);
        loanRepository.save(loan);
    }

    /** UML: approveLoan() — Business Rule: Only Manager */
    public void approveLoan(Long loanId, Long managerId, String remarks) {
        Loan loan = getById(loanId);
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        loan.approveLoan(manager, remarks);
        loanRepository.save(loan);
    }

    /** UML: rejectLoan() — Business Rule: Only Manager */
    public void rejectLoan(Long loanId, Long managerId, String remarks) {
        Loan loan = getById(loanId);
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        loan.rejectLoan(manager, remarks);
        loanRepository.save(loan);
    }

    public void disburseLoan(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.APPROVED)
            throw new IllegalStateException("Only approved loans can be disbursed");
        loan.disburse();
        loanRepository.save(loan);
    }

    public List<Loan> getCustomerLoans(Customer customer) {
        return loanRepository.findByCustomerOrderByAppliedDateDesc(customer);
    }

    public List<Loan> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.SUBMITTED);
    }

    public List<Loan> getUnderReviewLoans() {
        return loanRepository.findByStatus(LoanStatus.UNDER_REVIEW);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + id));
    }

    private String generateLoanRef() {
        return "LOAN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + String.format("%04d", new Random().nextInt(9999));
    }
}
