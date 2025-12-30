package com.example.demo.controller.admin;

import ch.qos.logback.core.model.Model;
import com.example.demo.models.Orders;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.example.demo.service.UserService;
import com.example.demo.service.checkout.OrderService;
import com.example.demo.service.profile.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderAdminController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    public OrderAdminController(OrderService orderService, UserService userService, ProductService productService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/admin/orders")
    public String orders(ModelMap model) {
        List<Orders> orders = orderService.getAllOrders();

        // Sort orders by orderDate in descending order (newest first)
        List<Orders> sortedOrders = orders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .collect(Collectors.toList());

        // Collect User IDs and Product IDs
        List<String> userIds = sortedOrders.stream()
                .map(Orders::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<String> productIds = sortedOrders.stream()
                .flatMap(order -> order.getItems().stream().map(item -> item.getProductId()))
                .distinct()
                .collect(Collectors.toList());

        // Fetch Users and Products
        Map<String, String> userIdToUsername = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Users::getUsername));
        Map<String, String> productIdToName = productService.getAllProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Products::getProduct_name));

        model.addAttribute("orders", sortedOrders);
        model.addAttribute("userIdToUsername", userIdToUsername);
        model.addAttribute("productIdToName", productIdToName);
        model.addAttribute("statusOptions", Arrays.asList(
                " ", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "PENDING", "FAILED_PAYMENT"));

        return "admin/orders/orders_dashboard";
    }

    @PostMapping("admin/orders/update-status")
    public String updateOrderStatus(@RequestParam String orderId,
                                    @RequestParam String newStatus) {
        orderService.updateOrderStatus(orderId, newStatus);
        return "redirect:/admin/orders";
    }
}
