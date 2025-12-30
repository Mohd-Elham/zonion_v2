//package com.example.demo.controller;
//
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//import org.json.JSONObject;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class RazorPayControlller {
//
//
//    private final RazorpayClient razorpayClient;
//
//    public RazorPayControlller(RazorpayClient razorpayClient) {
//        this.razorpayClient = razorpayClient;
//    }
//
//    @PostMapping("/create-razorpay-order")
//    public ResponseEntity<?> createRazorpayOrder(@RequestParam("amount") double amount,
//                                                 Authentication authentication) {
//            System.out.println("INSIDE RAZORPAY ORDER");
//        try {
//
//
//            org.json.JSONObject orderRequest = new JSONObject();
//            orderRequest.put("amount", amount * 100); // amount in paise
//            orderRequest.put("currency", "INR");
//            orderRequest.put("receipt", "order_rcptid_" + System.currentTimeMillis());
//
//            Order order = razorpayClient.orders.create(orderRequest);
//            return ResponseEntity.ok(order.toString());
//        } catch (RazorpayException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//}
