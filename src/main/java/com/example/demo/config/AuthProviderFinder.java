package com.example.demo.config;

import com.example.demo.models.Users;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthProviderFinder {

    private final UserService userService;

    public AuthProviderFinder(UserService userService) {
        this.userService = userService;
    }

    public Users getUserAuth(Authentication auth) {
        // If there is no authentication or it is not authenticated, return null.
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // If the user is authenticated via OAuth2 (e.g., Google)
        if (principal instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) principal;
            String email = oauthUser.getAttribute("email");
            return userService.findByEmail(email);
        }
        // If the user is authenticated via traditional form login
        else if (principal instanceof UserDetails) {
            String username = auth.getName();
            return userService.findByUsername(username);
        }
        // Return null if no matching condition is found
        return null;
    }
}
