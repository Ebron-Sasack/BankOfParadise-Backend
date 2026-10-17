package com.BankOfParadise.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record DepositRequest(

        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Deposit amount must be greater than zero"
        )
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        String description
) {}