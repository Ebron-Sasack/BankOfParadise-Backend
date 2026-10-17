package com.BankOfParadise.dto.response;

import com.BankOfParadise.enums.RoleType;
import com.BankOfParadise.enums.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        RoleType role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}