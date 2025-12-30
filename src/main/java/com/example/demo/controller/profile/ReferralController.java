package com.example.demo.controller.profile;


import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.WalletService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Controller
public class ReferralController {

    private final AuthProviderFinder authProviderFinder;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public ReferralController(AuthProviderFinder authProviderFinder, UserRepository userRepository, WalletService walletService) {
        this.authProviderFinder = authProviderFinder;
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @GetMapping("/profile/referrals")
    public String referrals(Authentication auth, Model model) {
        Users currentUser = authProviderFinder.getUserAuth(auth);


        if(currentUser.getReferralCode() == null){
            String referralCode = generateReferralCode(currentUser.getId());
            currentUser.setReferralCode(referralCode);
            userRepository.save(currentUser);

        }

        String currentUsersReferralCode = currentUser.getReferralCode();



        model.addAttribute("user", currentUser);
        model.addAttribute("referralCode", currentUsersReferralCode);
        return "main/profile/referral/referral_page";
    }

    public static String generateReferralCode(String userId){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(userId.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 8; i++){
                sb.append(Integer.toHexString((hash[i] & 0xff) % 16).toUpperCase());
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }



    @PostMapping("/apply-referral")
    @Transactional
    public String applyReferral(@RequestParam("referralCode") String referralCode,
            Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Users currentUser = authProviderFinder.getUserAuth(auth);

        if (currentUser.isReferralCodeUsed()){
            redirectAttributes.addFlashAttribute("error", "You've already used a referral code.");
            return "redirect:/";
        }

        Users referrer = userRepository.findByReferralCode(referralCode);
        if (referrer == null ){
            redirectAttributes.addFlashAttribute("error", "Referral code not found.");
            return "redirect:/";
        }

        referrer.setNumberOfReferrals(referrer.getNumberOfReferrals() + 1);
        walletService.addFunds(referrer, 100);

        currentUser.setReferralCodeUsed(true);
        currentUser.setReferralPromptShown(true);
        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("success", "Referral app lied! ₹100 credited to referrer.");
        redirectAttributes.addFlashAttribute("referralApplied", true);
        return "redirect:/";

    }

    @PostMapping("/referral-skip")
    public String skipReferral(Authentication auth, Model model, RedirectAttributes redirectAttributes) {
            Users currentUser = authProviderFinder.getUserAuth(auth);
            currentUser.setReferralPromptShown( true);
            userRepository.save(currentUser);

        System.out.println("INSIDE SKIP REFERRAL");
            return "redirect:/";
            }

}
