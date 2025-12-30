package com.example.demo.config;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
//@EnableWebSecurity
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // We inject UserRepository directly since we only need it for looking up users
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(username);
        System.out.println("Attempting to load user by email: " + username);

        if (user == null) {
            System.out.println("User not found: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        System.out.println("User found: " + user.getEmail());

        System.out.println("Found user: " + username);
        System.out.println("Stored password hash: " + user.getPassword());

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRoles())))
                .disabled(!user.isActive())
                .build();
    }
}