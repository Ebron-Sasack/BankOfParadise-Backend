package com.BankOfParadise.repository;

import com.BankOfParadise.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUser_Id(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}