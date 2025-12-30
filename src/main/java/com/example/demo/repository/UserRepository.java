package com.example.demo.repository;

import com.example.demo.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {
    Users findByUsername(String username);
    Users findByEmail(String email);

    Users findByReferralCode(String referralCode);
//    Optional<Users> findByEmail(String email);
}
