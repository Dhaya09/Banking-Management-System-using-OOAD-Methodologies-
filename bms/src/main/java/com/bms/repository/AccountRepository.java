package com.bms.repository;

import com.bms.model.Account;
import com.bms.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomer(Customer customer);
    List<Account> findByCustomerOrderByCreatedDateDesc(Customer customer);
    boolean existsByAccountNumber(String accountNumber);
}
