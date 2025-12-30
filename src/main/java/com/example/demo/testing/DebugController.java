package com.example.demo.testing;

import com.example.demo.config.PasswordConfig;
import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    @Autowired
    private PasswordConfig passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/debug-check-password")
    public String debugCheck(@RequestParam String username, @RequestParam String rawPassword) {
        Users user = userRepository.findByUsername(username);
        boolean match = passwordEncoder.passwordEncoder().matches(rawPassword, user.getPassword());
        return "Password matches: " + match;
    }
}