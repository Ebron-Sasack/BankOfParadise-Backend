package com.BankOfParadise.service.serviceImplentation;

import com.BankOfParadise.dto.request.AccountRequest;
import com.BankOfParadise.dto.response.AccountResponse;
import com.BankOfParadise.entity.Account;
import com.BankOfParadise.entity.User;
import com.BankOfParadise.enums.AccountStatus;
import com.BankOfParadise.repository.AccountRepository;
import com.BankOfParadise.repository.UserRepository;
import com.BankOfParadise.service.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AccountResponse createAccount(AccountRequest request) {

        // 1. Find the user
        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 2. Generate a unique account number
        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        // 3. Create the account
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .accountType(request.accountType())
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .status(AccountStatus.ACTIVE)
                .build();

        // 4. Save to database
        Account savedAccount = accountRepository.save(account);

        // 5. Return response DTO
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        return accountRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String generateAccountNumber() {

        StringBuilder number = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            number.append(secureRandom.nextInt(10));
        }

        return number.toString();
    }

    private AccountResponse mapToResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getUser().getId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
