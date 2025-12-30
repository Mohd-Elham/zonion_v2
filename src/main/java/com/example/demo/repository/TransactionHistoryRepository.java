package com.example.demo.repository;

import com.example.demo.models.wallet.TransactionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionHistoryRepository extends MongoRepository<TransactionHistory, String> {
    List<TransactionHistory> findByUserIdOrderByTimestampDesc(String userId);

    TransactionHistory findByUserId(String id);
}
