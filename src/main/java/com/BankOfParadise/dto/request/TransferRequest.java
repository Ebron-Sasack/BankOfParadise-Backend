package com.BankOfParadise.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "Source account number is required")
        String fromAccountNumber,

        @NotBlank(message = "Destination account number is required")
        String toAccountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Transfer amount must be greater than zero"
        )
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        String description
) {}