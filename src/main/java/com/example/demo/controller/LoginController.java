package com.example.demo.controller;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
//    @Autowired
//    private OtpService otpService;
//
//    @Autowired
//    private UserService userService;

    private OtpService otpService;
    private UserService userService;
    private UserRepository userRepository;

    public LoginController(OtpService otpService, UserService userService, UserRepository userRepository) {
        this.otpService = otpService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/signup")
    public String signup(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated())  {

            boolean isAdmin = authentication
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a
                            .getAuthority()
                            .equals("ROLE_ADMIN"));

            if(isAdmin) {
                return "redirect:/admin_dashboard";
            } else {
                return "redirect:/";
            }
        }

        return "signup_page";
    }

    @PostMapping("/signup")
    public String signupOTP(@RequestParam("username") String username,
                            @RequestParam("email") String email,
                            @RequestParam("password") String password,
                            @RequestParam("phone") String phone,
                            ModelMap model
                            ) {

        Users exsistingEmail = userRepository.findByEmail(email);

//        String exsitingUsername = userRepository.findByUsername(username).getUsername();
//
//        if (exsitingUsername != null && exsitingUsername.equals(username)) {
//            model.addAttribute("errorUsername", "Username already exists");
//            model.addAttribute("username", username);
//            model.addAttribute("email", email);
//            model.addAttribute("phone", phone);
//            return "signup_page";
//        }

        if (exsistingEmail != null) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "signup_page";
        }

        String otp =  otpService.generateOTP();
        otpService.sendMail(otp, email);

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("password", password);
        model.addAttribute("phone", phone);
        return "login/verify_otp";
    }

    @GetMapping("/verify_otp")
    public String verifyOTP() {
        System.out.println("sdlf");
        return "login/verify_otp";
    }

    @PostMapping("/verify_otp")
    public String verifyOTP(
            @RequestParam("otp") String otp,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            ModelMap model
    ) {
        System.out.println("Entered OTP: " + otp);

//        loginService.verifyOtp(otp, email);

        if (otpService.verifyOtp(otp, email)) {
            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setActive(true);

            session.setAttribute("username", username);

            userService.registerUser(username, password, email, phone);

            return "redirect:/login";

        }else{

            model.addAttribute("email", email);
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            model.addAttribute("phone", phone);
            return "redirect:/verify_otp?error=Invalid+OTP";
        }

//        return "login/verify_otp";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated())  {

            System.out.println("THIS IS A NEW CHANGE");

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Users user = userRepository.findByUsername(userDetails.getUsername());


            if (user == null) {

                System.out.println("user not found");

                SecurityContextHolder.clearContext();
                return "redirect:/login?error=user_not_found";
            }

            if(!user.isActive()) {
                SecurityContextHolder.clearContext();
                return "redirect:/login_page?error=blocked";
            }

            boolean isAdmin = authentication
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a
                            .getAuthority()
                            .equals("ROLE_ADMIN"));

            if(isAdmin) {
                return "redirect:/admin_dashboard";
            } else {
                return "redirect:/";
            }
        }

        return "login_page";
    }

//    forgot password logic

    @GetMapping("/forgot_password")
    public String forgotPassword() {
        return "login/forgot_password";
    }

    // In LoginController.java
    @GetMapping("/reset_password")
    public String showResetPassword(@RequestParam("email") String email, ModelMap model) {
        model.addAttribute("email", email);
        return "login/reset_password";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(@RequestParam("email") String email, ModelMap model) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return "redirect:/forgot_password?error=email_not_found";
        }

        String otp = otpService.generateOTP();
        otpService.sendPasswordResetOTP(otp, email);

        model.addAttribute("email", email);
        return "login/reset_password";
    }

//    reset pass word logic?

    @PostMapping("/reset_password")
    public String resetPassword(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp,
            @RequestParam("newPassword") String newPassword
    ) {
        if (!otpService.verifyPasswordResetOTP(otp, email)) {
            return "redirect:/reset_password?email=" + email + "&error=invalid_otp";
        }

        userService.setNewPassword(email, newPassword);

//        Users user = userRepository.findByEmail(email);
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepository.save(user);

        return "redirect:/login?reset_success";
    }

    @PostMapping("/resend_otp")
    public String resendOtp(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            ModelMap model) {

        String otp = otpService.generateOTP();
        otpService.sendMail(otp, email);

        model.addAttribute("email", email);
        model.addAttribute("username", username);
        model.addAttribute("phone", phone);
        model.addAttribute("password", password);

        return "login/verify_otp";
    }

    // In LoginController.java
    @GetMapping("/resend_reset_otp")
    public String resendResetOtp(@RequestParam("email") String email, ModelMap model) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return "redirect:/forgot_password?error=email_not_found";
        }

        String otp = otpService.generateOTP();
        otpService.sendPasswordResetOTP(otp, email);

        // Instead of using model.addAttribute (which doesn't work with redirects)
        return "redirect:/reset_password?email=" + email + "&resent=true";
    }

    @PostMapping("/resent_otp")
    public String resentOtp(
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            ModelMap model
    ){

        System.out.println("inside resent_otp");
        System.out.println("email: " + email
        + " phone: " + phone
        + " username: " + username
        + " password: " + password
        );

        String otp =  otpService.generateOTP();
        otpService.sendMail(otp, email);

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("password", password);
        model.addAttribute("phone", phone);
        model.addAttribute("resendSuccess", true); // Indicate resend success
        return "login/verify_otp";
    }


//    @PostMapping("/resend-signup-otp")
//    public String resendSignupOTP(
//            @RequestParam("email") String email,
//            @RequestParam("username") String username,
//            @RequestParam("password") String password,
//            @RequestParam("phone") String phone,
//            ModelMap model
//    ) {
//        String otp = otpService.generateOTP();
//        otpService.sendMail(otp, email);
//
//        model.addAttribute("username", username);
//        model.addAttribute("email", email);
//        model.addAttribute("password", password);
//        model.addAttribute("phone", phone);
//
//        return "login/verify_otp";
//    }


}
