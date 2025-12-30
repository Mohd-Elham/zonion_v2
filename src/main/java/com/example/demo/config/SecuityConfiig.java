package com.example.demo.config;

import com.example.demo.config.CheckUserStatusFilter;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomOAuth2UserService;
import com.example.demo.service.CustomOidUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.InetAddress;

@Configuration
@EnableWebSecurity
public class SecuityConfiig  {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
//    private CustomOAuth2UserService customOAuth2UserService;
//    private CustomOidcUserService
    private CustomOidUserService customOidUserService;
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    public SecuityConfiig(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder,
                          CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, CustomOidUserService customOidUserService
                        , CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customOidUserService = customOidUserService;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }


    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry, UserRepository userRepository) throws Exception {

        http.
                csrf(csrf -> csrf.disable()).
                addFilterBefore(new CheckUserStatusFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                .requestMatchers("/admin_dashboard/**").hasRole("ADMIN")
                                .requestMatchers("/profile/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/",
                                "/admin_login",
//                                "/admin_dashboard",
//                                "/admin_dashboard/add_products",
//                                "/admin_dashboard/add_category",
//                                "/admin_dashboard/category",
                                "/hello123",
                                "/duummy",
                                "/category/**",
                                "/static/uploads/**",
                                "/main/**",
                                "/signup",
                                "/categories/add",
                                "/login**",
                                "/error**",
                                "/forgot_password",
                                "/reset_password",
                                "/reset_password**",
                                "/resend_reset_otp",
                                "/resend_reset_otp",
                                "/verify_otp",
                                "/resent_otp",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**",
                                "/assets/**",
                                "/fonts/**",
                                "/verify_otp",
//                                "/favicon.ico",
                                "/resources/**",
                                "/static/**",
                                "/public/**",
                                "/dist/**",           // For built assets
                                "/build/**",          // For built assets
                                "/*.js",              // Root JavaScript files
                                "/*.css",             // Root CSS files
                                "/*.ico",             // Favicon and other icons
                                "/*.png",             // Images
                                "/*.jpg",
                                "/*.svg",             // SVG files
                                "/*.json",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/webjars/**",// JSON files (like manifest)
                                "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                                .defaultSuccessUrl("/")
//                        .successHandler(customAuthenticationSuccessHandler)
//                        .userInfoEndpoint((Customizer<OAuth2LoginConfigurer<HttpSecurity>.UserInfoEndpointConfig>) customOidUserService)
                )
                .formLogin(form ->form
                                .loginPage("/login")
//                                .defaultSuccessUrl("/", true)
                                .successHandler(customAuthenticationSuccessHandler)
                                .usernameParameter("email")
//                                .failureUrl("/login?error=true")
                                .failureHandler(customAuthenticationFailureHandler)
                                .permitAll()
                        )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()

                )
                .sessionManagement( session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?error=sessionexpired")
                        .sessionRegistry(sessionRegistry));


        System.out.println("This is the secuity");
        String localhost = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Localhost value: " + localhost);

                return http.build();
    }

}
