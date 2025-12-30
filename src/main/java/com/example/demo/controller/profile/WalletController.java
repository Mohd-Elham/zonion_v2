package com.example.demo.controller.profile;

import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.RazorDTO;
import com.example.demo.models.Users;
import com.example.demo.models.wallet.TransactionHistory;
import com.example.demo.models.wallet.TransactionStatus;
import com.example.demo.models.wallet.Wallet;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.RazorPayService;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.rmi.MarshalledObject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Controller
public class WalletController {

    private final AuthProviderFinder authProviderFinder;
    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final RazorPayService razorPayService;

    public WalletController(AuthProviderFinder authProviderFinder, WalletRepository walletRepository, TransactionHistoryRepository transactionHistoryRepository, RazorPayService razorPayService) {
        this.authProviderFinder = authProviderFinder;
        this.walletRepository = walletRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.razorPayService = razorPayService;
    }

    @GetMapping("/profile/wallet")
    public String wallet(Authentication auth, Model model) {
        Users currentUser = authProviderFinder.getUserAuth(auth);
        String userId = currentUser.getId();

        // Create wallet if not exists
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            wallet = new Wallet();
            String randUUID = java.util.UUID.randomUUID().toString();
            wallet.setWalletId(randUUID);
            wallet.setUserId(userId);
            wallet.setBalance(0.0);
            wallet = walletRepository.save(wallet);
        }

        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions",
                transactionHistoryRepository.findByUserIdOrderByTimestampDesc(userId));
        return "main/profile/wallet/wallet_page";
    }


    @PostMapping("/create-razorpay-wallet-order")
    @ResponseBody
    public ResponseEntity<RazorDTO> createRazorpayOrder(@RequestBody Map<String, Integer> orderRequest, Authentication auth) throws RazorpayException {
        Users currentUser = authProviderFinder.getUserAuth(auth);
        String userId = currentUser.getId();
        String name = currentUser.getUsername();
        String email = currentUser.getEmail();

    System.out.println("INSIDE CREATE RAZORPAY ORDER");
        System.out.println("order request: " + orderRequest);

        RazorDTO razorDTO = razorPayService.createOrder(orderRequest, name, email);

        return new ResponseEntity<RazorDTO>(razorDTO, HttpStatus.OK);
    }

    @PostMapping("wallet/add-money")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addMoney(
            @RequestBody Map<String, String> request,
            Authentication auth) throws RazorpayException {

        Users currentUser = authProviderFinder.getUserAuth(auth);
        String userId = currentUser.getId();

        // Extract payment details
        String amountStr = request.get("amount");
        String razorpayPaymentId = request.get("razorpayPaymentId");
        String razorpayOrderId = request.get("razorpayOrderId");

        // Convert amount to rupees
        double amountInPaise = Double.parseDouble(amountStr);
        double amountInRupees = amountInPaise / 100.0;

        // Update wallet balance
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            wallet = createNewWallet(userId, amountInRupees);
        } else {
            wallet.setBalance(wallet.getBalance() + amountInRupees);
            walletRepository.save(wallet);
        }

        // Create and save transaction history
        TransactionHistory transaction = new TransactionHistory();
        String randUUID = java.util.UUID.randomUUID().toString();
        transaction.setId(randUUID);
        transaction.setRazorpayId(razorpayPaymentId);
        transaction.setAmount(String.valueOf(amountInRupees));
        transaction.setStatus("success");
        transaction.setUserId(userId);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.INCOMING);

        transactionHistoryRepository.save(transaction);

        return ResponseEntity.ok().body(Map.of("status", "success"));
    }

    private Wallet createNewWallet(String userId, double initialBalance) {
        Wallet wallet = new Wallet();
        wallet.setWalletId(UUID.randomUUID().toString());
        wallet.setUserId(userId);
        wallet.setBalance(initialBalance);
        return walletRepository.save(wallet);
    }




}
