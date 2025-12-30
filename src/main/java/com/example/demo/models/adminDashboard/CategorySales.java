package com.example.demo.models.adminDashboard;

public class CategorySales {
    private String categoryName;
    private int quantity;

    public CategorySales(String categoryName, int quantity) {
        this.categoryName = categoryName;
        this.quantity = quantity;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
