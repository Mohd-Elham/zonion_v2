package com.example.demo.controller.profile;

import com.example.demo.config.AuthProviderFinder;
import com.example.demo.config.CustomUserDetailsService;
import com.example.demo.models.Address;
import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OtpService;
import com.example.demo.service.ProfileService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthProviderFinder authProviderFinder;

    public ProfileController(ProfileService profileService, CustomUserDetailsService customUserDetailsService, UserRepository userRepository, OtpService otpService, PasswordEncoder passwordEncoder, UserService userService, AuthProviderFinder authProviderFinder) {
        this.profileService = profileService;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.authProviderFinder = authProviderFinder;
    }

    @GetMapping("/profile")
    public String profile(ModelMap model, Authentication auth, HttpSession session) {

        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users exisitingUser = profileService.getUsername(currentUser.getUsername());

        System.out.println("Exisiting user: " + exisitingUser);

        if (exisitingUser == null) {
            // Handle user not found scenario, e.g., redirect to login
            return "redirect:/login";
        }


        // ***KEY CHANGE: Ensure auth_provider is set in the existingUser object***
        if (currentUser.getAuth_provider() != null) {  // Check if currentUser has auth_provider
            exisitingUser.setAuth_provider(currentUser.getAuth_provider()); // Copy it
        }
//        System.out.println(exisitingUser.toString());
        model.addAttribute("user", exisitingUser);
        session.setAttribute("username", exisitingUser);

        return "main/profile/profile_page";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("id") String id,
            @RequestParam("username") String newUsername,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            ModelMap model, Authentication auth, HttpSession session) {

        Users existingUserWithNewUsername = userRepository.findByUsername(newUsername);
        if (existingUserWithNewUsername != null && !existingUserWithNewUsername.getId().equals(id)) {
            model.addAttribute("error", "Username already taken");
            return "main/profile/profile_page";
        }

        profileService.updateUsernameEmailPhone(id, newUsername, email, phone);
        System.out.println("Database updated for user ID: " + id + ", new username: " + newUsername); // Log database update

        Users updatedUser = profileService.getUsersById(id);
        System.out.println("Fetched updated user from DB: " + updatedUser); // Log fetched user
        if (updatedUser == null) {
            return "redirect:/profile?error=user_not_found";
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(updatedUser.getEmail());
        System.out.println("Reloaded UserDetails with username: " + userDetails.getUsername()); // Log reloaded UserDetails

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                auth.getCredentials(),
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        System.out.println("Security Context updated with new authentication for username: " + SecurityContextHolder.getContext().getAuthentication().getName()); // Log Security Context update

        session.setAttribute("username", updatedUser);
        System.out.println("Session updated with username: " + updatedUser.getUsername()); // Log session update

        return "redirect:/profile";
    }

//    address setup
    @GetMapping("/profile/address")
    public String address(ModelMap model, Authentication auth, HttpSession session) {

        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users exisitingUser = profileService.getUsername(currentUser.getUsername());
        List<Address> addresses = profileService.getAddressById(exisitingUser.getId());
        System.out.println("Fetched addresses from DB: " + addresses);

        model.addAttribute("addresses", addresses);
        model.addAttribute("user", exisitingUser.getUsername());
        model.addAttribute("userId", exisitingUser.getId());

        return "main/profile/address/address1";
    }


    @GetMapping("/profile/change_password/{email}")
    public String forgotPassword(@PathVariable String email, ModelMap model){
        Users user = profileService.getUserByEmail(email);
        if (user == null) {
            // Handle invalid email (e.g., redirect to an error page or show a message)
            return "redirect:/profile?error=User+not+found";
        }


        // Add the email to the model so it can be displayed on the forgot password page
        model.addAttribute("email", email);

        // Render the forgot password page
        return "main/profile/change_password";
    }

    @PostMapping("/profile/reset_password")
    public String handlePasswordReset(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        Users user = profileService.getUserByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/profile/change_password/" + email;
        }



        // Check if old password is correct
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Old password is incorrect.");
            return "redirect:/profile/change_password/" + email;
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile/change_password/" + email;
        }

        // Check if new password is the same as the old password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "New password cannot be the same as the old password.");
            return "redirect:/profile/change_password/" + email;
        }

        // Update the password
        userService.setNewPasswordd(email, newPassword);

        redirectAttributes.addFlashAttribute("success", "Password updated successfully!");
        return "redirect:/profile";
    }


}
