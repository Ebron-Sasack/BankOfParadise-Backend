package com.BankOfParadise.dto.response;

import com.BankOfParadise.enums.AccountStatus;
import com.BankOfParadise.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        Long userId,
        AccountType accountType,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}