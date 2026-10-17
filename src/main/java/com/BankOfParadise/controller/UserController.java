package com.BankOfParadise.controller;

import com.BankOfParadise.dto.request.EmailOtpVerifyRequest;
import com.BankOfParadise.dto.request.EmailUpdateRequest;
import com.BankOfParadise.dto.request.UserRequest;
import com.BankOfParadise.dto.response.UserResponse;
import com.BankOfParadise.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Only admin or authorized bank employees can create customers.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Retrieve the currently authenticated user.
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        UserResponse response =
                userService.getCurrentUser(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // Step 1: Request OTP for the new email address.
    @PostMapping("/me/email/request-otp")
    public ResponseEntity<Map<String, String>> requestEmailUpdate(
            Authentication authentication,
            @Valid @RequestBody EmailUpdateRequest request) {

        userService.requestEmailUpdate(
                authentication.getName(),
                request.getNewEmail()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "If the request is eligible, an email verification OTP has been sent."
        ));
    }

    // Step 2: Verify OTP and update the email.
    @PostMapping("/me/email/verify")
    public ResponseEntity<UserResponse> verifyEmailUpdate(
            Authentication authentication,
            @Valid @RequestBody EmailOtpVerifyRequest request) {

        UserResponse response = userService.verifyEmailUpdate(
                authentication.getName(),
                request.getNewEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok(response);
    }
}