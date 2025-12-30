package com.example.demo.repository;

import com.example.demo.models.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MyCartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);
}
