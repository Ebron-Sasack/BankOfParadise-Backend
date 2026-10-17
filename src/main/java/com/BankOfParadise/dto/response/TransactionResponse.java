package com.BankOfParadise.dto.response;

import com.BankOfParadise.enums.TransactionStatus;
import com.BankOfParadise.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,
        String transactionReference,
        String accountNumber,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt
) {}