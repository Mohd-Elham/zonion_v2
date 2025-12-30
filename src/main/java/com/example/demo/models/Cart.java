package com.example.demo.models;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

public class Cart {
    @Id
    private String id;

    private String userId;
    private List<CartItems> items;
    private double totalPrice;
    private double offerDiscountedValue; // New field

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    private double finalPrice; // New field

    private Map<String, String> appliedOffers; // Product/Category ID -> Offer ID

    public double getOfferDiscountedValue() {
        return offerDiscountedValue;
    }

    public void setOfferDiscountedValue(double offerDiscountedValue) {
        this.offerDiscountedValue = offerDiscountedValue;
    }

    public Map<String, String> getAppliedOffers() {
        return appliedOffers;
    }

    public void setAppliedOffers(Map<String, String> appliedOffers) {
        this.appliedOffers = appliedOffers;
    }

    private double couponDiscountedValue;
    private String couponCode;

    public double getCouponDiscountedValue() {
        return couponDiscountedValue;
    }

    public void setCouponDiscountedValue(double discountedValue) {
        this.couponDiscountedValue = discountedValue;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Cart(String id, String userId, List<CartItems> items, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItems> getItems() {
        return items;
    }

    public void setItems(List<CartItems> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", items=" + items +
                '}';
    }


    public static class CartItems {
        private String productId;
        private int quantity;
        private double price;

        public CartItems(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        public String toString() {
            return "CartItems{" +
                    "productId='" + productId + '\'' +
                    ", quantity=" + quantity +
                    ", price=" + price +
                    '}';
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
