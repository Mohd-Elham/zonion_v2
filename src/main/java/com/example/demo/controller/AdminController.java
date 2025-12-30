package com.example.demo.controller;

import com.example.demo.models.InventoryHistory;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.example.demo.service.InventoryHistoryService;
import com.example.demo.service.UserService;
import com.example.demo.service.profile.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
public class AdminController {

    private final SessionRegistry sessionRegistry;
    private UserService userService;
    private ProductService productService;
    private InventoryHistoryService inventoryHistoryService;


    public AdminController(UserService userService, SessionRegistry sessionRegistry,
                           InventoryHistoryService inventoryHistoryService,ProductService productService) {
        this.userService = userService;
        this.sessionRegistry = sessionRegistry;
        this.inventoryHistoryService = inventoryHistoryService;
        this.productService = productService;
    }

//    @GetMapping("/admin_dashboard")
//    public String adminDashboard(ModelMap model) {
//        List<Users> users = userService.getAllUsers();
//
//        model.addAttribute("users", users);
//
//
//        return "admin/admin_dashboard";
//    }

    @GetMapping("/admin_dashboard")
    public String adminDashboard(
            @RequestParam(defaultValue = "1") int page,
            ModelMap model) {

        // Adjust page to 0-based index
        int adjustedPage = page - 1;
        if (adjustedPage < 0) adjustedPage = 0;

        // Fetch a page of users with 10 items per page
        Page<Users> userPage = userService.getAllUserss(PageRequest.of(adjustedPage, 6));

        System.out.println(userPage.toString());

        // Add data to the model
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());

        return "admin/admin_dashboard";
    }

    //this is used for blocking and unblocking user
@PostMapping("/admin/toggle-user/{id}")
    public String blockUser(@PathVariable String id) {
        Users user = userService.findById(id);


        userService.updateStatus(user);

        if (!user.isActive()) { // Assuming 'isActive' returns false when blocked.
            sessionRegistry.getAllPrincipals().forEach(principal -> {
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    // Assuming your Users entity's username matches the UserDetails username
                    if (userDetails.getUsername().equals(user.getUsername())) {
                        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                        sessions.forEach(SessionInformation::expireNow);
                    }
                }
            });
        }
        return "redirect:/admin_dashboard";
    }

    // In UserController.java



}
