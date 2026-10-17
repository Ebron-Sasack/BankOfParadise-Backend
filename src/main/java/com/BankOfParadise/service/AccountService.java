
package com.BankOfParadise.service;

import com.BankOfParadise.dto.request.AccountRequest;
import com.BankOfParadise.dto.response.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // Account creation and retrieval
    AccountResponse createAccount(AccountRequest request);

    AccountResponse getAccountById(Long accountId);

    List<AccountResponse> getAccountsByUserId(Long userId);

    AccountResponse getAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAllAccounts();

    // Balance inquiry
    BigDecimal getBalance(Long accountId);

    // Banking operations
    AccountResponse deposit(Long accountId, BigDecimal amount);

    AccountResponse withdraw(Long accountId, BigDecimal amount);

    void transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount
    );

    // Account management
    AccountResponse closeAccount(Long accountId);

    AccountResponse updateAccountStatus(
            Long accountId,
            String status
    );
}
