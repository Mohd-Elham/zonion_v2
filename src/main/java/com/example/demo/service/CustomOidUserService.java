package com.example.demo.service;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOidUserService extends OidcUserService {
    private UserRepository userRepository;

    public CustomOidUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Use traditional null check instead of Optional
        Users user = userRepository.findByEmail(email);

        if(user == null) {
            user = new Users();
            user.setEmail(email);
            user.setUsername(name);
            user.setAuth_provider("GOOGLE");
            user.setActive(true);
            user.setRoles("ROLE_USER");
            userRepository.save(user);
        }

        // Create Spring Security UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("") // Empty password for OAuth users
                .roles(user.getRoles())
                .build();

        return new DefaultOidcUser(
                userDetails.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
    }
}
