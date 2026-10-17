package com.BankOfParadise.service;

import com.BankOfParadise.dto.request.DepositRequest;
import com.BankOfParadise.dto.request.TransferRequest;
import com.BankOfParadise.dto.request.WithdrawalRequest;
import com.BankOfParadise.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse deposit(
            String phoneNumber,
            DepositRequest request
    );

    TransactionResponse withdraw(
            String phoneNumber,
            WithdrawalRequest request
    );

    List<TransactionResponse> transfer(
            String phoneNumber,
            TransferRequest request
    );

    List<TransactionResponse> getTransactionHistory(
            String phoneNumber,
            String accountNumber
    );

    TransactionResponse getTransactionByReference(
            String phoneNumber,
            String transactionReference
    );
}