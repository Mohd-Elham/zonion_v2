package com.example.demo.models;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Wishlists {

    @Id
    private String id;
    private List<String> productId;
    private String userId;


    public Wishlists(String id, List<String> productId, String userId) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getProductId() {
        return productId;
    }

    public void setProductId(List<String> productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
