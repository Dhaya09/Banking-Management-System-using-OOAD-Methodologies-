package com.bms.repository;

import com.bms.model.Account;
import com.bms.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);
    Optional<Transaction> findByTransactionReference(String ref);
    List<Transaction> findTop10ByAccountOrderByTransactionDateDesc(Account account);
}
