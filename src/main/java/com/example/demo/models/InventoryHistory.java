package com.example.demo.models;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class InventoryHistory {

    @Id
    private String id;
    private String productId;
    private int previousStock;
    private int newStock;
    private int adjustment;
    private String notes;
    private LocalDateTime date;

    public InventoryHistory() {
    }

    public InventoryHistory(String id, String productId, int previousStock, int newStock, int adjustment, String notes, LocalDateTime date) {
        this.id = id;
        this.productId = productId;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.adjustment = adjustment;
        this.notes = notes;
        this.date = date;
    }

    public InventoryHistory(String productId, int previousStock, int newStock, String notes) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(int previousStock) {
        this.previousStock = previousStock;
    }

    public int getNewStock() {
        return newStock;
    }

    public void setNewStock(int newStock) {
        this.newStock = newStock;
    }

    public int getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(int adjustment) {
        this.adjustment = adjustment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
