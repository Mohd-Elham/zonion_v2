package com.example.demo.repository;

import com.example.demo.models.Wishlists;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WishlistRepository extends MongoRepository<Wishlists, String> {
    Optional<Wishlists> findByUserId(String userId);
}
