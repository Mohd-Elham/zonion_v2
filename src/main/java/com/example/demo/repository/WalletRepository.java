package com.example.demo.repository;

import com.example.demo.models.wallet.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    Wallet findByUserId(String userId);
}
