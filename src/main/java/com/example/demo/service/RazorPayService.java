package com.example.demo.service;


import com.example.demo.models.Orders;
import com.example.demo.models.RazorDTO;
import com.example.demo.repository.OrderRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class RazorPayService {

    private final OrderRepository orderRepository;
    RazorpayClient client;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    public RazorPayService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void init() throws RazorpayException {
        if (apiKey == null || apiSecret == null) {
            throw new RuntimeException("Razorpay API Key and Secret are not set");
        }
        this.client = new RazorpayClient(apiKey, apiSecret);
    }

    public RazorDTO createOrder(Map<String, Integer> orderRequest, String username, String email) throws RazorpayException {




        org.json.JSONObject orderJson = new JSONObject();
        orderJson.put("amount", orderRequest.get("amount")); // paise into rupees
        orderJson.put("currency", "INR");
        orderJson.put("receipt", "order_rcpt_" + System.currentTimeMillis());

        Order orders = client.orders.create(orderJson);
//
//        orderss.setRazorpayId(orders.get("id"));
//        orderss.setStatus(orders.get("status"));
//        String randUUID = java.util.UUID.randomUUID().toString();
//        orderss.setId(randUUID);

        RazorDTO orderss = new RazorDTO();
        orderss.setAmount(orderRequest.get("amount"));
        orderss.setRazorpayId(orders.get("id"));
        orderss.setName(username);
        orderss.setEmail(email);

        //here rest of the orders setting

        return  orderss;
    }

}
