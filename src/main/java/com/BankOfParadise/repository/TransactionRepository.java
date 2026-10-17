package com.BankOfParadise.repository;

import com.BankOfParadise.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );

    List<Transaction> findByAccount_AccountNumberOrderByCreatedAtDesc(
            String accountNumber
    );
}