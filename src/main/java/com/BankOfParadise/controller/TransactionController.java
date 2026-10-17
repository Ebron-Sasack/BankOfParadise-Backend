package com.BankOfParadise.controller;

import com.BankOfParadise.dto.request.DepositRequest;
import com.BankOfParadise.dto.request.TransferRequest;
import com.BankOfParadise.dto.request.WithdrawalRequest;
import com.BankOfParadise.dto.response.TransactionResponse;
import com.BankOfParadise.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Deposit money
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request) {

        TransactionResponse response =
                transactionService.deposit(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Withdraw money
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawalRequest request) {

        TransactionResponse response =
                transactionService.withdraw(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Transfer money between accounts
    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponse>> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {

        List<TransactionResponse> responses =
                transactionService.transfer(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }

    // View account transaction history
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            Authentication authentication,
            @PathVariable String accountNumber) {

        List<TransactionResponse> responses =
                transactionService.getTransactionHistory(
                        authentication.getName(),
                        accountNumber
                );

        return ResponseEntity.ok(responses);
    }

    // Find a transaction by its reference
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponse> getByReference(
            Authentication authentication,
            @PathVariable String reference) {

        TransactionResponse response =
                transactionService.getTransactionByReference(
                        authentication.getName(),
                        reference
                );

        return ResponseEntity.ok(response);
    }
}