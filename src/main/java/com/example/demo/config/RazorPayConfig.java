//package com.example.demo.config;
//
//
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class RazorPayConfig {
//
//    @Value("${razorpay.api.key}")
//    public String razorpayKey;
//
//    @Value("${razorpay.api.secret}")
//    public String razorpaySecret;
//
//    @Bean
//    public RazorpayClient razorpayClient() throws RazorpayException {
//        return new RazorpayClient(razorpayKey, razorpaySecret);
//    }
//
//}
