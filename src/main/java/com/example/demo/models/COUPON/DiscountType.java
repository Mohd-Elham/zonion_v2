package com.example.demo.models.COUPON;
public enum DiscountType {
    FLAT("Flat"),
    PERCENTAGE("Percentage");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
