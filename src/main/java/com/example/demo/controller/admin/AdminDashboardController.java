package com.example.demo.controller.admin;

import com.example.demo.models.Orders;
import com.example.demo.models.Products;
import com.example.demo.models.adminDashboard.CategorySales;
import com.example.demo.models.adminDashboard.ProductSales;
import com.example.demo.service.checkout.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Controller
public class AdminDashboardController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public AdminDashboardController(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/salary")
    public String salary(ModelMap model) {


//        model.addAttribute("salary", salary);
        return "/salary";
    }

    @PostMapping("/salary_increase")
    public String salaryIncrease(@RequestParam("salary") int salary,ModelMap model) {

        double increasedAmount = salary * .10;
        double increasedSalary =salary + increasedAmount;
        model.addAttribute("salary", increasedSalary);
        return "/salary/increased";
    }

    // OrdersController.java
    @GetMapping("/admin_dashboard/main")
    public String adminDashboard(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            ModelMap model) throws JsonProcessingException {

        if (period != null) {
            period = "daily";
        }

        if (startDate == null) {
            startDate = LocalDate.of(2023, 2, 10);
        }

        // Default endDate: 2025-02-23 if not provided
        if (endDate == null) {
            endDate = LocalDate.now().plusDays(1);
        }

        LocalDateTime[] dateRange = calculateDateRange(period, startDate, endDate);
        List<Orders> deliveredOrders = orderService.getDeliveredOrdersInRange(dateRange[0], dateRange[1]);

        // Chart Data
        Map<String, Double> chartData = orderService.getSalesByPeriod(deliveredOrders, period);
        String chartDataJson = objectMapper.writeValueAsString(chartData);
        model.addAttribute("chartDataJson", chartDataJson);

        // Top Products
        List<ProductSales> topProducts = orderService.getTopProducts(deliveredOrders, 10);
        ObjectMapper mapper = new ObjectMapper();
        String topProductsJson = mapper.writeValueAsString(topProducts);
        System.out.println(topProducts.toString());
        System.out.println("INSIDE TOP PRODUCTS JSON: " + topProductsJson);
        model.addAttribute("topProductsData", topProductsJson);

        // Top Categories
        List<CategorySales> topCategories = orderService.getTopCategories(deliveredOrders, 10);
        ObjectMapper mapper2 = new ObjectMapper();
        String topCategoriesJson = mapper2.writeValueAsString(topCategories);
        model.addAttribute("topCategoriesJson", topCategoriesJson);

        // Sales Metrics Calculations
        long totalOrders = deliveredOrders.size();
        double totalSales = deliveredOrders.stream().mapToDouble(Orders::getTotalPrice).sum();
        double totalCouponDiscounts = deliveredOrders.stream().mapToDouble(Orders::getCouponDiscount).sum();
        double totalOfferDiscounts = deliveredOrders.stream().mapToDouble(Orders::getOfferDiscount).sum();
        int totalProductsSold = deliveredOrders.stream()
                .flatMapToInt(o -> o.getItems().stream().mapToInt(Orders.OrderItem::getQuantity))
                .sum();

        // Add metrics to model
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalCouponDiscounts", totalCouponDiscounts);
        model.addAttribute("totalOfferDiscounts", totalOfferDiscounts);
        model.addAttribute("totalProductsSold", totalProductsSold);


        return "admin/admin_dashboard/main_admin_dashboard_page";
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

}
