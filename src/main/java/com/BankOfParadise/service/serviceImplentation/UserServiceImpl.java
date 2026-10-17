package com.BankOfParadise.service.serviceImplentation;

import com.BankOfParadise.dto.request.UserRequest;
import com.BankOfParadise.dto.response.UserResponse;
import com.BankOfParadise.entity.User;
import com.BankOfParadise.enums.RoleType;
import com.BankOfParadise.enums.UserStatus;
import com.BankOfParadise.repository.UserRepository;
import com.BankOfParadise.service.UserService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // Create a customer profile by an authorized admin/employee
    @Override
    public UserResponse createUser(UserRequest request) {

        String email = normalizeEmail(request.email());
        String phone = request.phoneNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (userRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException(
                    "Phone number is already registered"
            );
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .phoneNumber(phone)
                .password(null)
                .role(RoleType.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // Get a user by ID
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        return mapToResponse(user);
    }

    // Get the currently authenticated user
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String phoneNumber) {

        User user = getUserByPhoneNumber(phoneNumber);

        return mapToResponse(user);
    }

    // Update email after successful OTP verification in Auth Service
    @Override
    public UserResponse updateEmail(
            String phoneNumber,
            String newEmail) {

        User user = getUserByPhoneNumber(phoneNumber);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Inactive users cannot update their email"
            );
        }

        String normalizedEmail = normalizeEmail(newEmail);

        if (normalizedEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException(
                    "New email must be different from current email"
            );
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        user.setEmail(normalizedEmail);

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private User getUserByPhoneNumber(String phoneNumber) {

        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse mapToResponse(User user) {

        return modelMapper.map(user, UserResponse.class);
    }
}