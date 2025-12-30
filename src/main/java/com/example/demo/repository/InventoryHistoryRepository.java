package com.example.demo.repository;

import com.example.demo.models.InventoryHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryHistoryRepository extends MongoRepository<InventoryHistory, String> {
    List<InventoryHistory> findByProductIdOrderByDateDesc(String productId);

//    List<InventoryHistory> findAllByDateDesc();

//    List<InventoryHistory> findAll();
}
