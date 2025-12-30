package com.example.demo.controller;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/loggedInUsers")
public class SessionRegistaryController {
    private final SessionRegistry sessionRegistry;
    private final UserService userService;


    @Autowired
    public SessionRegistaryController(SessionRegistry sessionRegistry, UserService userService) {
        this.sessionRegistry = sessionRegistry;
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<List<String>> getActiveUsers() {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        List<String> activeUsers = new ArrayList<>();

        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                activeUsers.add(username);
                // Also print to the console
                System.out.println("Active User: " + username);
            } else if (principal instanceof OAuth2User) {
                String username = ((OAuth2User) principal).getAttribute("email");
                String user = userService.findByEmail(username).getUsername();
                activeUsers.add(user);
                System.out.println("Active User: " + username);
            }
        }

        return ResponseEntity.ok(activeUsers);
    }
}
