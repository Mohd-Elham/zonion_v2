package com.example.demo.service;

import com.example.demo.models.Users;
import com.example.demo.models.wallet.TransactionHistory;
import com.example.demo.models.wallet.TransactionStatus;
import com.example.demo.models.wallet.Wallet;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;
    WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository, TransactionHistoryRepository transactionHistoryRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.userRepository = userRepository;
    }

    public Wallet getWallet(String userId){
        return walletRepository.findByUserId(userId);
    }

    public void saveWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }

    public void addFunds(Users referrer, int money) {
        Wallet wallet = walletRepository.findByUserId(referrer.getId());
        if(wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(referrer.getId());
            wallet.setBalance(money);
        }
        wallet.setBalance(wallet.getBalance() + money);
        saveWallet(wallet);

        TransactionHistory transaction = new TransactionHistory();
        String randUUID = java.util.UUID.randomUUID().toString();
        transaction.setId(randUUID);
        transaction.setAmount(String.valueOf(money));
        transaction.setStatus("Referral code");
        transaction.setUserId(referrer.getId());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.INCOMING);

        transactionHistoryRepository.save(transaction);

        userRepository.save(referrer);
    }
}
