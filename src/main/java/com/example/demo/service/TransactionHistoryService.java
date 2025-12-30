package com.example.demo.service;


import com.example.demo.models.wallet.TransactionHistory;
import com.example.demo.repository.TransactionHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionHistoryService {

    TransactionHistoryRepository transactionHistoryRepository;

    public TransactionHistoryService(TransactionHistoryRepository transactionHistoryRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
    }


    public TransactionHistory getTransactionHistoryOfWallet(String id) {
        return transactionHistoryRepository.findByUserId(id);
    }
}
