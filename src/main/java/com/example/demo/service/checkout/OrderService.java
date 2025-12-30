package com.example.demo.service.checkout;

import com.example.demo.models.*;
import com.example.demo.models.adminDashboard.CategorySales;
import com.example.demo.models.adminDashboard.ProductSales;
import com.example.demo.repository.MyAddressRepo;
import com.example.demo.repository.MyCartRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.profile.ProductService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final MyCartRepository myCartRepository;
    private final MyAddressRepo myAddressRepo;
    private final ProductService productService;
    private final CategoryService categoryService;
    OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository, MyCartRepository myCartRepository, MyAddressRepo myAddressRepo, ProductService productService, CategoryService categoryService) {
        this.orderRepository = orderRepository;
        this.myCartRepository = myCartRepository;
        this.myAddressRepo = myAddressRepo;
        this.productService = productService;
        this.categoryService = categoryService;
    }


    public Orders setNewOrder(String userId, String selectedAddress, PaymentType paymentMethod, Cart cart) {

        Address address = myAddressRepo.findById(selectedAddress).orElseThrow(() -> new RuntimeException("Address not found"));

        List<Orders.OrderItem> orderItemList = cart.getItems().stream()
                .map(item -> new Orders.OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();
        Orders order = new Orders();
        order.setRazorpayId(null);
        order.setId(generateOrderId());
        order.setUserId(userId);
        order.setItems(orderItemList);
        order.setTotalPrice(cart.getTotalPrice() - cart.getCouponDiscountedValue() - cart.getOfferDiscountedValue());
        order.setCouponDiscount(cart.getCouponDiscountedValue());
        order.setOfferDiscount(cart.getOfferDiscountedValue());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
//        order.setDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setShippingAddress(selectedAddress);
        order.setPaymentMethod(paymentMethod);
        orderRepository.save(order);

        return order;
    }

    public String generateOrderId() {
        SecureRandom random = new SecureRandom();
        int randomNumber = 100000000 + random.nextInt(900000000);
        return "OD" + randomNumber;
    }

    public Orders getOrderById(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Orders> getOrdersByUserId(String id) {
        return orderRepository.findByUserId(id);
    }

    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public List<Orders> getActiveOrdersByUserId(String id) {
        return orderRepository.findByUserIdAndStatusNot(id, "DELIVERED");
    }

    public void cancelOrder(String orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getStatus().equals("DELIVERED")) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }
    }

    public List<Orders> getActiveOrders(String id) {
        return orderRepository.findByUserIdAndStatusIn(
                id,
                List.of("PENDING", "SHIPPED", "PROCESSING", "FAILED_PAYMENT")
        );
    }

    public List<Orders> getCompletedOrdersByUserId(String userId) {
        return orderRepository.findByUserIdAndStatusIn(
                userId,
                List.of("DELIVERED", "CANCELLED", "RETURNED")
        );
    }

    public void cancelOrderr(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals("DELIVERED")) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);

            // Restore stock for each item
            for (Orders.OrderItem item : order.getItems()) {
                productService.increaseStock(
                        item.getProductId(),
                        item.getQuantity()
                );
            }
        }
    }

    public Orders setNewOrderRazorpay(String razorpayId, String userId, String selectedAddress, PaymentType paymentMethod, Cart cart) {
        Address address = myAddressRepo.findById(selectedAddress).orElseThrow(() -> new RuntimeException("Address not found"));

        List<Orders.OrderItem> orderItemList = cart.getItems().stream()
                .map(item -> new Orders.OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();
        Orders order = new Orders();
        order.setRazorpayId(razorpayId);
        order.setId(generateOrderId());
        order.setUserId(userId);
        order.setItems(orderItemList);
        order.setTotalPrice(cart.getTotalPrice() - cart.getCouponDiscountedValue() - cart.getOfferDiscountedValue());
        order.setCouponDiscount(cart.getCouponDiscountedValue());
        order.setOfferDiscount(cart.getOfferDiscountedValue());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
//        order.setDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setShippingAddress(selectedAddress);
        order.setPaymentMethod(PaymentType.RAZORPAY);
        orderRepository.save(order);

        return order;
    }

    public List<Orders> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }

    public void returnOrder(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("DELIVERED")) {
            order.setStatus("RETURNED");
            orderRepository.save(order);

            // Restore stock for each item
            for (Orders.OrderItem item : order.getItems()) {
                productService.increaseStock(
                        item.getProductId(),
                        item.getQuantity()
                );
            }
        }
    }

    public List<Orders> getDeliveredOrdersInRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetweenAndStatus(start, end, "DELIVERED");
    }

    private String getPeriodKey(LocalDateTime date, String period) {
        return switch (period != null ? period.toLowerCase() : "monthly") {
            case "yearly" -> String.valueOf(date.getYear());
            case "monthly" -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            case "weekly" -> date.getYear() + "-W" + String.format("%02d",
                    date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case "daily" -> date.toLocalDate().toString();
            default -> "All";
        };
    }
    public Map<String, Double> getSalesByPeriod(List<Orders> deliveredOrders, String period) {
        Map<String, Double> salesData = new LinkedHashMap<>();

        for (Orders order : deliveredOrders) {
            String periodKey = getPeriodKey(order.getOrderDate(), period);
            salesData.merge(periodKey, order.getTotalPrice(), Double::sum);
        }
        return salesData;
    }

    public List<ProductSales> getTopProducts(List<Orders> orders, int limit) {
        Map<String, Integer> productQuantities = new HashMap<>();
        System.out.println("INSIDE GET TOP PRODUCTS" + orders.toString());
        // Get all product IDs
        List<String> productIds = orders.stream()
                .flatMap(o -> o.getItems().stream().map(Orders.OrderItem::getProductId))
                .collect(Collectors.toList());
        System.out.println("productIds: " + productIds);

        // Get product names
        Map<String, String> productNames = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Products::getProduct_name));
        System.out.println("productNames: " + productNames);

        // Calculate quantities
        orders.forEach(o -> o.getItems().forEach(item ->
                productQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum)));

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new ProductSales(
                        productNames.getOrDefault(entry.getKey(), "Unknown Product"),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<CategorySales> getTopCategories(List<Orders> orders, int limit) {
        Map<String, Integer> categoryCounts = new HashMap<>();

        // Get product-category mapping
        List<String> productIds = orders.stream()
                .flatMap(o -> o.getItems().stream().map(Orders.OrderItem::getProductId))
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> productCategoryIds  = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Products::getCategory));

        Set<String> categoryIds = new HashSet<>(productCategoryIds.values());

        Map<String, String> categoryIdToName = categoryService.getCategoriesByIds2(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategory_name));

        // Calculate category quantities
        orders.forEach(o -> o.getItems().forEach(item -> {
            String categoryId = productCategoryIds.getOrDefault(item.getProductId(), "unknown");
            String categoryName = categoryIdToName.getOrDefault(categoryId, "Unknown Category");
            categoryCounts.merge(categoryName, item.getQuantity(), Integer::sum);
        }));

        return categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new CategorySales(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Orders createFailedPaymentOrder(String id, Cart cart) {
        Orders order = new Orders();
        order.setId(generateOrderId());
        order.setUserId(id);
        order.setItems(convertCartItems(cart.getItems()));
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus("FAILED_PAYMENT");
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private List<Orders.OrderItem> convertCartItems(List<Cart.CartItems> cartItems) {
        return cartItems.stream()
                .map(item -> new Orders.OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}
