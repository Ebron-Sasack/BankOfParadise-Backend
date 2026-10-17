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

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    private static final String DEFAULT_CURRENCY = "INR";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Create a new bank account
    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {

        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required to create an account"
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Account account = new Account();

        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(DEFAULT_CURRENCY);
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // Get account by ID
    @Override
    public AccountResponse getAccountById(Long accountId) {

        Account account = findAccountById(accountId);

        return mapToResponse(account);
    }

    // Get all accounts belonging to a user
    @Override
    public List<AccountResponse> getAccountsByUserId(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        return accountRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Find account using account number
    @Override
    public AccountResponse getAccountByAccountNumber(
            String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required"
            );
        }

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        return mapToResponse(account);
    }

    // Get all accounts
    @Override
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Check account balance
    @Override
    public BigDecimal getBalance(Long accountId) {

        Account account = findAccountById(accountId);

        ensureAccountIsActive(account);

        return account.getBalance();
    }

    // Deposit money
    @Override
    @Transactional
    public AccountResponse deposit(
            Long accountId,
            BigDecimal amount) {

        validateAmount(amount);

        Account account = findAccountById(accountId);

        ensureAccountIsActive(account);

        account.setBalance(account.getBalance().add(amount));

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // Withdraw money
    @Override
    @Transactional
    public AccountResponse withdraw(
            Long accountId,
            BigDecimal amount) {

        validateAmount(amount);

        Account account = findAccountById(accountId);

        ensureAccountIsActive(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        account.setBalance(account.getBalance().subtract(amount));

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // Transfer money between two accounts
    @Override
    @Transactional
    public void transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {

        validateAmount(amount);

        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException(
                    "Both account IDs are required"
            );
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        Account sourceAccount = findAccountById(fromAccountId);
        Account destinationAccount = findAccountById(toAccountId);

        ensureAccountIsActive(sourceAccount);
        ensureAccountIsActive(destinationAccount);

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance for transfer"
            );
        }

        // Deduct from the source account
        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount)
        );

        // Credit the destination account
        destinationAccount.setBalance(
                destinationAccount.getBalance().add(amount)
        );

        // Both updates participate in the same transaction
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
    }

    // Close an account instead of deleting its database record
    @Override
    @Transactional
    public AccountResponse closeAccount(Long accountId) {

        Account account = findAccountById(accountId);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed"
            );
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Account balance must be zero before closure"
            );
        }

        account.setStatus(AccountStatus.CLOSED);

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // Update account status
    @Override
    @Transactional
    public AccountResponse updateAccountStatus(
            Long accountId,
            String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Account status is required"
            );
        }

        Account account = findAccountById(accountId);

        AccountStatus newStatus;

        try {
            newStatus = AccountStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid account status: " + status
            );
        }

        if (account.getStatus() == AccountStatus.CLOSED
                && newStatus != AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "A closed account cannot be reactivated"
            );
        }

        account.setStatus(newStatus);

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // Find an account or throw an exception
    private Account findAccountById(Long accountId) {

        if (accountId == null) {
            throw new IllegalArgumentException(
                    "Account ID is required"
            );
        }

        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));
    }

    // Validate deposit, withdrawal, and transfer amounts
    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than two decimal places"
            );
        }
    }

    // Check whether an account can perform transactions
    private void ensureAccountIsActive(Account account) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }
    }

    // Generate a unique 16-digit account number
    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber = String.format(
                    "%016d",
                    RANDOM.nextLong(1_000_000_000_000_000L,
                            10_000_000_000_000_000L)
            );
        } while (accountRepository
                .existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // Convert Account entity to AccountResponse DTO
    private AccountResponse mapToResponse(Account account) {

        return modelMapper.map(account, AccountResponse.class);
    }
}
