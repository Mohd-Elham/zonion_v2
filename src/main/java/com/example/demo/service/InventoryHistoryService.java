package com.example.demo.service;

import com.example.demo.models.InventoryHistory;
import com.example.demo.repository.InventoryHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryHistoryService {

    private final InventoryHistoryRepository inventoryHistoryRepository;

    public InventoryHistoryService(InventoryHistoryRepository inventoryHistoryRepository) {
        this.inventoryHistoryRepository = inventoryHistoryRepository;
    }

    public void recordInventoryChange(String productId, int previousStock, int newStock, int adjustment, String notes) {
        InventoryHistory history = new InventoryHistory();
        history.setId(UUID.randomUUID().toString());
        history.setProductId(productId);
        history.setPreviousStock(previousStock);
        history.setNewStock(newStock);
        history.setAdjustment(adjustment);
        history.setNotes(notes);
        history.setDate(LocalDateTime.now());
        inventoryHistoryRepository.save(history);
    }

    public List<InventoryHistory> getHistoryByProduct(String productId) {
        return inventoryHistoryRepository.findByProductIdOrderByDateDesc(productId);
    }

    public List<InventoryHistory> getAllHistory() {
        return inventoryHistoryRepository.findAll();    }
}
