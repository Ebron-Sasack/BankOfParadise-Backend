package com.BankOfParadise.service;

import com.BankOfParadise.dto.request.AccountRequest;
import com.BankOfParadise.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    AccountResponse getAccountById(Long accountId);

    List<AccountResponse> getAccountsByUserId(Long userId);
}
