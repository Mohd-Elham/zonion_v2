package com.example.demo.config;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CheckUserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public CheckUserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("CheckUserStatusFilter invoked for URL: " + request.getRequestURI());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            System.out.println("CheckUserStatusFilter invoked for principal: " + principal);
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }
            else if (principal instanceof OAuth2User) {
                String email = ((OAuth2User) principal).getAttribute("email");
                username = userRepository.findByEmail(email).getUsername();
                System.out.println("OAuth2User attributes: " + ((OAuth2User) principal).getAttributes());
//                username =((OAuth2User) principal).getName();
            }
            if (username != null) {
                Users user = userRepository.findByUsername(username);
                if (user != null) {
                    System.out.println("User " + username + " active status: " + user.isActive());
                    if (!user.isActive()) { // User is blocked
                        // Clear authentication and invalidate session
                        SecurityContextHolder.clearContext();
                        request.getSession().invalidate();
                        // Redirect immediately to login page with an error message
                        System.out.println("User " + username + " is blocked. Redirecting to login.");
                        response.sendRedirect(request.getContextPath() + "/login?blocked="+username+"blocked");
                        return;
                    }
                }
            }
        }
        // Proceed with the filter chain if user is active
        filterChain.doFilter(request, response);
    }
}
