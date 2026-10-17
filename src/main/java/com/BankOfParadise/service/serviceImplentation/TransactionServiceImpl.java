package com.BankOfParadise.service.serviceImplentation;

import com.BankOfParadise.dto.request.DepositRequest;
import com.BankOfParadise.dto.request.TransferRequest;
import com.BankOfParadise.dto.request.WithdrawalRequest;
import com.BankOfParadise.dto.response.TransactionResponse;
import com.BankOfParadise.entity.Account;
import com.BankOfParadise.entity.Transaction;
import com.BankOfParadise.enums.AccountStatus;
import com.BankOfParadise.enums.TransactionStatus;
import com.BankOfParadise.enums.TransactionType;
import com.BankOfParadise.repository.AccountRepository;
import com.BankOfParadise.repository.TransactionRepository;
import com.BankOfParadise.service.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ---------------- DEPOSIT ----------------

    @Override
    public TransactionResponse deposit(
            String phoneNumber,
            DepositRequest request) {

        validateAmount(request.amount());

        Account account = getLockedAccount(request.accountNumber());

        validateActiveAccount(account);
        validateOwnership(account, phoneNumber);

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.amount());

        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = createTransaction(
                account,
                TransactionType.DEPOSIT,
                request.amount(),
                balanceBefore,
                balanceAfter,
                request.description()
        );

        return mapToResponse(
                transactionRepository.save(transaction)
        );
    }

    // ---------------- WITHDRAWAL ----------------

    @Override
    public TransactionResponse withdraw(
            String phoneNumber,
            WithdrawalRequest request) {

        validateAmount(request.amount());

        Account account = getLockedAccount(request.accountNumber());

        validateActiveAccount(account);
        validateOwnership(account, phoneNumber);

        BigDecimal balanceBefore = account.getBalance();

        if (balanceBefore.compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        BigDecimal balanceAfter =
                balanceBefore.subtract(request.amount());

        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = createTransaction(
                account,
                TransactionType.WITHDRAWAL,
                request.amount(),
                balanceBefore,
                balanceAfter,
                request.description()
        );

        return mapToResponse(
                transactionRepository.save(transaction)
        );
    }

    // ---------------- TRANSFER ----------------

    @Override
    public List<TransactionResponse> transfer(
            String phoneNumber,
            TransferRequest request) {

        validateAmount(request.amount());

        String fromNumber = request.fromAccountNumber();
        String toNumber = request.toAccountNumber();

        if (fromNumber.equals(toNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        /*
         * Lock accounts in a consistent order to reduce the risk
         * of deadlocks when transfers occur simultaneously.
         */
        String firstNumber = fromNumber.compareTo(toNumber) < 0
                ? fromNumber : toNumber;

        String secondNumber = fromNumber.compareTo(toNumber) < 0
                ? toNumber : fromNumber;

        Account firstAccount = getLockedAccount(firstNumber);
        Account secondAccount = getLockedAccount(secondNumber);

        Account fromAccount = fromNumber.equals(firstNumber)
                ? firstAccount : secondAccount;

        Account toAccount = toNumber.equals(firstNumber)
                ? firstAccount : secondAccount;

        validateActiveAccount(fromAccount);
        validateActiveAccount(toAccount);

        validateOwnership(fromAccount, phoneNumber);

        BigDecimal senderBalanceBefore = fromAccount.getBalance();
        BigDecimal receiverBalanceBefore = toAccount.getBalance();

        if (senderBalanceBefore.compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        BigDecimal senderBalanceAfter =
                senderBalanceBefore.subtract(request.amount());

        BigDecimal receiverBalanceAfter =
                receiverBalanceBefore.add(request.amount());

        // Update both balances.
        fromAccount.setBalance(senderBalanceAfter);
        toAccount.setBalance(receiverBalanceAfter);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String description = request.description() == null
                || request.description().isBlank()
                ? "Fund transfer"
                : request.description().trim();

        // Record the outgoing side of the transfer.
        Transaction debitTransaction = createTransaction(
                fromAccount,
                TransactionType.TRANSFER,
                request.amount(),
                senderBalanceBefore,
                senderBalanceAfter,
                "Transfer to " + toNumber + ": " + description
        );

        // Record the incoming side of the transfer.
        Transaction creditTransaction = createTransaction(
                toAccount,
                TransactionType.TRANSFER,
                request.amount(),
                receiverBalanceBefore,
                receiverBalanceAfter,
                "Transfer from " + fromNumber + ": " + description
        );

        List<Transaction> savedTransactions =
                transactionRepository.saveAll(
                        List.of(debitTransaction, creditTransaction)
                );

        return savedTransactions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ---------------- TRANSACTION HISTORY ----------------

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(
            String phoneNumber,
            String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        )
                );

        validateOwnership(account, phoneNumber);

        return transactionRepository
                .findByAccount_AccountNumberOrderByCreatedAtDesc(
                        accountNumber
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ---------------- GET BY REFERENCE ----------------

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(
            String phoneNumber,
            String transactionReference) {

        Transaction transaction = transactionRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transaction not found"
                        )
                );

        validateOwnership(transaction.getAccount(), phoneNumber);

        return mapToResponse(transaction);
    }

    // ---------------- HELPER METHODS ----------------

    private Account getLockedAccount(String accountNumber) {

        return accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found: " + accountNumber
                        )
                );
    }

    private void validateActiveAccount(Account account) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Account is not active: "
                            + account.getAccountNumber()
            );
        }
    }

    private void validateOwnership(
            Account account,
            String phoneNumber) {

        if (account.getUser() == null
                || account.getUser().getPhoneNumber() == null
                || !account.getUser().getPhoneNumber()
                .equals(phoneNumber)) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to access this account"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than two decimal places"
            );
        }
    }

    private Transaction createTransaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description) {

        return Transaction.builder()
                .transactionReference(
                        UUID.randomUUID().toString()
                )
                .account(account)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(TransactionStatus.SUCCESS)
                .description(normalizeDescription(description))
                .build();
    }

    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        String normalized = description.trim();

        if (normalized.length() > 255) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 255 characters"
            );
        }

        return normalized;
    }

    private TransactionResponse mapToResponse(
            Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getAccount().getAccountNumber(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}