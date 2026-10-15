package com.BankOfParadise.controller;

import com.BankOfParadise.dto.request.AccountRequest;
import com.BankOfParadise.dto.response.AccountResponse;
import com.BankOfParadise.payload.ApiResponse;
import com.BankOfParadise.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // Create a new account
    @PostMapping
    public ResponseEntity<ApiResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity.ok(new ApiResponse("Account Created",response));
    }

    // Get an account by its ID
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                accountService.getAccountById(accountId)
        );
    }

    // Get all accounts belonging to a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                accountService.getAccountsByUserId(userId)
        );
    }
}