package com.example.demo.models.wallet;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class TransactionHistory {

    @Id
    private String id;
    private String razorpayId;


    private String amount;
    private String status;
    private String userId;
    private LocalDateTime timestamp;
    private TransactionStatus transactionStatus;

    public TransactionHistory() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRazorpayId() {
        return razorpayId;
    }

    public void setRazorpayId(String razorpayId) {
        this.razorpayId = razorpayId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public TransactionHistory(String id, String razorpayId, String amount, String status, LocalDateTime timestamp, TransactionStatus transactionStatus) {
        this.id = id;
        this.razorpayId = razorpayId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.transactionStatus = transactionStatus;
    }
}
