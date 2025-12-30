package com.example.demo.service;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract user information from Google response
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Check if user exists in your database
        Users user = userRepository.findByEmail(email);

        if(user == null) {
            String randomId = UUID.randomUUID().toString();

            // Register new user
            user = new Users();
            user.setId(randomId);
            user.setEmail(email);
            user.setUsername(name);
            user.setAuth_provider("GOOGLE");
            user.setActive(true);
            // Set default role or determine based on your logic
            user.setCreated_at(System.currentTimeMillis());
            user.setRoles("ROLE_USER");
            userRepository.save(user);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoles())),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}