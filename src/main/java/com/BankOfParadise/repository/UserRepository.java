package com.BankOfParadise.repository;

import com.BankOfParadise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phone);

    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
