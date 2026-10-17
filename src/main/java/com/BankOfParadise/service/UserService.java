package com.BankOfParadise.service;

import com.BankOfParadise.dto.request.UserRequest;
import com.BankOfParadise.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long userId);

    UserResponse getCurrentUser(String phoneNumber);

    UserResponse updateEmail(
            String phoneNumber,
            String newEmail
    );
}
