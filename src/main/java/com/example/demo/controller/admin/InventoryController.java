package com.example.demo.controller.admin;

import com.example.demo.models.InventoryHistory;
import com.example.demo.models.Products;
import com.example.demo.service.InventoryHistoryService;
import com.example.demo.service.profile.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InventoryController {


    private final ProductService productService;
    private final InventoryHistoryService inventoryHistoryService;

    public InventoryController(ProductService productService, InventoryHistoryService inventoryHistoryService) {
        this.productService = productService;
        this.inventoryHistoryService = inventoryHistoryService;
    }

    @GetMapping("/admin_dashboard/inventory")
    public String inventoryManagement(ModelMap model) {

        List<Products> products = productService
                .getAllProducts();
        List<InventoryHistory> history = inventoryHistoryService.getAllHistory();
        List<InventoryHistory> reversedHistory = new ArrayList<>(history);
        Collections.reverse(reversedHistory);

        Map<String, Products> productsMap = products.stream()
                        .collect(Collectors.toMap(Products::getId, p -> p));

        List<Map<String, Object>> historyWithProducts = new ArrayList<>();
        for (InventoryHistory entry : reversedHistory) {
            Map<String, Object> historyEntry = new HashMap<>();
            historyEntry.put("history", entry);
            historyEntry.put("product", productsMap.get(entry.getProductId())); // Fetch corresponding product
            historyWithProducts.add(historyEntry);
        }
        System.out.println("history" + history);
//        System.out.println("historyWithProducts: " + historyWithProducts.get(0));
        model.addAttribute("products", products);
        model.addAttribute("history", historyWithProducts);
        return "admin/inventory/inventory";
    }

    @PostMapping("/admin_dashboard/inventory/adjust")
    public String adjustStock(
            @RequestParam String productId,
            @RequestParam String action,
            @RequestParam int quantity,
            @RequestParam String notes
    ) {
        int adjustment = action.equals("subtract") ? -quantity : quantity;
        productService.adjustStock(productId, adjustment, notes);
        return "redirect:/admin_dashboard/inventory";
    }
}
