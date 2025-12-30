package com.example.demo.controller.profile;

import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.*;
import com.example.demo.models.wallet.TransactionHistory;
import com.example.demo.models.wallet.TransactionStatus;
import com.example.demo.models.wallet.Wallet;
import com.example.demo.repository.MyAddressRepo;
import com.example.demo.repository.ProductsRepository;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.service.*;
import com.example.demo.service.checkout.OrderService;
import com.example.demo.service.profile.ProductService;
import com.example.demo.service.profile.addressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class OrdersController {
    private final ProfileService profileService;
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final com.example.demo.service.profile.addressService addressService;
    private final MyAddressRepo myAddressRepo;
    private final AuthProviderFinder authProviderFinder;
    private final SalesReportPdfService salesReportPdfService;
    private final WalletService walletService;
    private final TransactionHistoryService transactionHistoryService;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProductsRepository productsRepository;
    private final SalesReportExcelService salesReportExcelService;


    public OrdersController(ProfileService profileService, OrderService orderService, UserService userService, ProductService productService, addressService addressService, MyAddressRepo myAddressRepo, AuthProviderFinder authProviderFinder, SalesReportPdfService salesReportPdfService, WalletService walletService, TransactionHistoryService transactionHistoryService, TransactionHistoryRepository transactionHistoryRepository, ProductsRepository productsRepository, SalesReportExcelService salesReportExcelService) {
        this.profileService = profileService;
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
        this.addressService = addressService;
        this.myAddressRepo = myAddressRepo;
        this.authProviderFinder = authProviderFinder;
        this.salesReportPdfService = salesReportPdfService;
        this.walletService = walletService;
        this.transactionHistoryService = transactionHistoryService;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.productsRepository = productsRepository;
        this.salesReportExcelService = salesReportExcelService;
    }

    @GetMapping("/profile/orders")
    public String orders(ModelMap model, Authentication auth, HttpSession session) throws JsonProcessingException {
        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users existingUser = profileService.getUsername(currentUser.getUsername());
        if (existingUser == null) {
            return "redirect:/login"; // Handle user not found
        }

        List<Orders> orders = orderService.getCompletedOrdersByUserId(existingUser.getId());

        List<Orders> sortedOrders = orders.stream()
                .sorted(Comparator.comparing(Orders::getOrderDate).reversed())
                .collect(Collectors.toList());
        // Create map for address IDs to formatted addresses
        Map<String, String> addressMap = new HashMap<>();
        Set<String> productIds = sortedOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(Orders.OrderItem::getProductId)
                .collect(Collectors.toSet());

        List<Products> products = productsRepository.findAllById(productIds);

        for (Orders order : sortedOrders) {
            Address address = myAddressRepo.findById(order.getShippingAddress()).orElse(null);

            if (address != null) {
                addressMap.put(order.getShippingAddress(), address.getFormattedAddress());
            } else {
                addressMap.put(order.getShippingAddress(), "Address not found");
            }
        }
//
//        Map<String, String> productMap = productsRepository.findAllById(productIds).stream()
//                .collect(Collectors.toMap(Products::getId, Products::getProduct_name));

        Map<String, String> productMap = products.stream()
                .collect(Collectors.toMap(
                        Products::getId,
                        Products::getProduct_name,
                        (existing, replacement) -> existing));

        Map<String, String> orderProductsMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();


        // In your controller method
        for (Orders order : sortedOrders) {
            List<Map<String, Object>> items = order.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("name", productMap.getOrDefault(item.getProductId(), "Product not found"));
                        itemMap.put("quantity", item.getQuantity());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            orderProductsMap.put(order.getId(), objectMapper.writeValueAsString(items));
        }

        model.addAttribute("orderProductsMap", orderProductsMap);
        model.addAttribute("orders", sortedOrders);
        model.addAttribute("addressMap", addressMap);
        model.addAttribute("orderProductsMap", orderProductsMap);
        model.addAttribute("user", existingUser);// To keep user info in nav bar

        return "main/profile/orders/orders_page"; // Return the name of your orders.html template
    }

    @GetMapping("/orders")
    public String activeOrders(ModelMap model, Authentication auth) {
        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users user = userService.getIdByUsername(currentUser.getUsername());

        List<Orders> activeOrders = orderService.getActiveOrders(user.getId());

        // Collect all product IDs from all order items
        List<String> productIds = activeOrders.stream()
                .flatMap(order -> order.getItems().stream().map(item -> item.getProductId()))
                .collect(Collectors.toList());
        Map<String, String> productNames = productService
                .getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Products::getProduct_name));

        // Collect all shipping address IDs from orders
        List<String> addressIds = activeOrders.stream()
                .map(Orders::getShippingAddress)
                .collect(Collectors.toList());
        Map<String, Address> addressMap = addressService.getAddressesByIds(addressIds).stream()
                .collect(Collectors.toMap(Address::getId, Function.identity()));

        model.addAttribute("orders", activeOrders);
        model.addAttribute("productNames", productNames);
        model.addAttribute("addresses", addressMap);

        return "main/orders/main_orders_page";
    }


    //this is for active orders
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable String orderId, Authentication auth) {
        Users user = authProviderFinder.getUserAuth(auth);
        Orders order = orderService.getOrderById(orderId);
        double orderTotal = order.getTotalPrice();

        if(order.getPaymentMethod() == PaymentType.WALLET || order.getPaymentMethod() == PaymentType.RAZORPAY){
            Wallet usersWallet = walletService.getWallet(user.getId());
            usersWallet.setBalance(usersWallet.getBalance() + orderTotal);
            walletService.saveWallet(usersWallet);

            TransactionHistory newTranscation = new TransactionHistory();
            newTranscation.setId(UUID.randomUUID().toString());
            newTranscation.setAmount(String.valueOf(orderTotal));
            newTranscation.setStatus("refund");
            newTranscation.setUserId(user.getId());
            newTranscation.setTimestamp(LocalDateTime.now());
            newTranscation.setTransactionStatus(TransactionStatus.INCOMING);

            usersWallet.setBalance(usersWallet.getBalance() + orderTotal);
            walletService.saveWallet(usersWallet);
            transactionHistoryRepository.save(newTranscation);
        }


        System.out.println(user.getId() + " " + orderTotal);

        if (order != null && order.getUserId().equals(user.getId())) {
            orderService.cancelOrderr(orderId);
        }
        return "redirect:/orders";
    }

    @PostMapping("/profile/orders/return")
    public String returnOrder(@RequestParam String orderId, Authentication auth) throws AccessDeniedException {
        Users user = authProviderFinder.getUserAuth(auth);
        Orders order = orderService.getOrderById(orderId);


        if (!order.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Not your order");
        }

        double orderTotal = order.getTotalPrice();

        if(order.getPaymentMethod() == PaymentType.WALLET || order.getPaymentMethod() == PaymentType.RAZORPAY){
            Wallet usersWallet = walletService.getWallet(user.getId());
            usersWallet.setBalance(usersWallet.getBalance() + orderTotal);
            walletService.saveWallet(usersWallet);

            TransactionHistory newTranscation = new TransactionHistory();
            newTranscation.setId(UUID.randomUUID().toString());
            newTranscation.setAmount(String.valueOf(orderTotal));
            newTranscation.setStatus("refund");
            newTranscation.setUserId(user.getId());
            newTranscation.setTimestamp(LocalDateTime.now());
            newTranscation.setTransactionStatus(TransactionStatus.INCOMING);

            usersWallet.setBalance(usersWallet.getBalance() + orderTotal);
            walletService.saveWallet(usersWallet);
            transactionHistoryRepository.save(newTranscation);
        }


        System.out.println(user.getId() + " " + orderTotal);

            orderService.returnOrder(orderId);
        return "redirect:/profile/orders";

    }

    @GetMapping("/admin_dashboard/sales-report")
    public String salesReport(@RequestParam(required = false) String period,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {

        LocalDateTime[] dateRange = calculateDateRange(period, startDate, endDate);
        List<Orders> allOrders  = orderService.getOrdersBetweenDates(dateRange[0], dateRange[1]);
        List<Orders> deliveredOrders = allOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());


        List<String> userIds = deliveredOrders.stream()
                .map(Orders::getUserId)
                .collect(Collectors.toList());
        Map<String, Users> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));

        // Get product details
        List<String> productIds = deliveredOrders.stream()
                .flatMap(order -> order.getItems().stream().map(Orders.OrderItem::getProductId))
                .collect(Collectors.toList());
        Map<String, Products> productMap = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Function.identity()));

        long totalOrders = deliveredOrders.size();
        double totalSales = deliveredOrders.stream().mapToDouble(Orders::getTotalPrice).sum();


        double totalCouponDiscounts = deliveredOrders.stream().mapToDouble(Orders::getCouponDiscount).sum();
        double totalOfferDiscounts = deliveredOrders.stream().mapToDouble(Orders::getOfferDiscount).sum();


        int totalProductsSold = deliveredOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .flatMapToInt(o -> o.getItems().stream().mapToInt(Orders.OrderItem::getQuantity))
                .sum();

        model.addAttribute("totalProductsSold", totalProductsSold);
        model.addAttribute("orders", deliveredOrders);
        model.addAttribute("userMap", userMap);
        model.addAttribute("productMap", productMap);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalCouponDiscounts", totalCouponDiscounts);
        model.addAttribute("totalOfferDiscounts", totalOfferDiscounts);

        return "admin/sales report/sales_report_dashboard";
    }

    @GetMapping("/admin_dashboard/sales-report/pdf")
    public ResponseEntity<byte[]> generateSalesReportPdf(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws DocumentException, DocumentException {

        // Get data using existing logic
        LocalDateTime[] dateRange = calculateDateRange(period, startDate, endDate);
        List<Orders> allOrders = orderService.getOrdersBetweenDates(dateRange[0], dateRange[1]);
        List<Orders> deliveredOrders = allOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());

        // ... rest of data retrieval logic from original method ...


        List<String> userIds = deliveredOrders.stream()
                .map(Orders::getUserId)
                .collect(Collectors.toList());
        Map<String, Users> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));

        // Get product details
        List<String> productIds = deliveredOrders.stream()
                .flatMap(order -> order.getItems().stream().map(Orders.OrderItem::getProductId))
                .collect(Collectors.toList());
        Map<String, Products> productMap = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Function.identity()));

        long totalOrders = deliveredOrders.size();
        double totalSales = deliveredOrders.stream().mapToDouble(Orders::getTotalPrice).sum();


        double totalCouponDiscounts = deliveredOrders.stream().mapToDouble(Orders::getCouponDiscount).sum();
        double totalOfferDiscounts = deliveredOrders.stream().mapToDouble(Orders::getOfferDiscount).sum();


        int totalProductsSold = deliveredOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .flatMapToInt(o -> o.getItems().stream().mapToInt(Orders.OrderItem::getQuantity))
                .sum();

        // Generate PDF
        byte[] pdfBytes = salesReportPdfService.generateSalesReportPdf(
                deliveredOrders,
                totalOrders,
                totalSales,
                totalCouponDiscounts,
                totalOfferDiscounts,
                totalProductsSold,
                userMap,
                productMap
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("sales_report.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private LocalDateTime[] calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        if (period != null) {
            switch (period.toLowerCase()) {
                case "today":
                    start = LocalDate.now().atStartOfDay();
                    break;
                case "week":
                    start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                    break;
                case "month":
                    start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    break;
                case "year":
                    start = LocalDate.now().withDayOfYear(1).atStartOfDay();
                    break;
            }
        }

        if (startDate != null) {
            start = startDate.atStartOfDay();
        }
        if (endDate != null) {
            end = endDate.atTime(LocalTime.MAX);
        }

        return new LocalDateTime[]{start, end};
    }

    @GetMapping("/admin_dashboard/sales-report/excel")
    public ResponseEntity<byte[]> generateSalesReportExcel(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException, IOException {

        // Same data retrieval logic as PDF endpoint
        LocalDateTime[] dateRange = calculateDateRange(period, startDate, endDate);
        List<Orders> allOrders = orderService.getOrdersBetweenDates(dateRange[0], dateRange[1]);
        List<Orders> deliveredOrders = allOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());

        List<String> userIds = deliveredOrders.stream()
                .map(Orders::getUserId)
                .collect(Collectors.toList());
        Map<String, Users> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));


        // ... rest of data retrieval logic ...
        List<String> productIds = deliveredOrders.stream()
                .flatMap(order -> order.getItems().stream().map(Orders.OrderItem::getProductId))
                .collect(Collectors.toList());
        Map<String, Products> productMap = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Function.identity()));

        long totalOrders = deliveredOrders.size();
        double totalSales = deliveredOrders.stream().mapToDouble(Orders::getTotalPrice).sum();


        double totalCouponDiscounts = deliveredOrders.stream().mapToDouble(Orders::getCouponDiscount).sum();
        double totalOfferDiscounts = deliveredOrders.stream().mapToDouble(Orders::getOfferDiscount).sum();


        int totalProductsSold = deliveredOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .flatMapToInt(o -> o.getItems().stream().mapToInt(Orders.OrderItem::getQuantity))
                .sum();

        byte[] excelBytes = salesReportExcelService.generateSalesReportExcel(
                deliveredOrders,
                totalOrders,
                totalSales,
                totalCouponDiscounts,
                totalOfferDiscounts,
                totalProductsSold,
                userMap,
                productMap
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("sales_report.xlsx").build());
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
