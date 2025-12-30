package com.example.demo.models.wallet;

import org.springframework.data.annotation.Id;

public class Wallet {
    @Id
    private String walletId;
    private double balance;
    private String userId;
    private String transactionHistoryId;

    public Wallet(String walletId, double balance, String userId, String transactionHistoryId) {
        this.walletId = walletId;
        this.balance = balance;
        this.userId = userId;
        this.transactionHistoryId = transactionHistoryId;
    }

    public Wallet() {
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransactionHistoryId() {
        return transactionHistoryId;
    }

    public void setTransactionHistoryId(String transactionHistoryId) {
        this.transactionHistoryId = transactionHistoryId;
    }
}
