package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    @Autowired
    private JavaMailSender mailSender;

    Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

    private static final int OTP_LENGTH = 4;
    private static final int OTP_DURATION = 5 * 60 * 1000;// 5 mins


    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateOTP() {
        Random random = new Random();
        int otp = random.nextInt(9000) + 1000; // 4 digit pin
        System.out.println("OTP: " + otp);


        return String.valueOf(otp);

    }
    public void sendMail(String otp, String email) {
        String normalizedEmail = email.toLowerCase();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("muhammedelham@gmail.com");
        message.setTo(email);
        message.setSubject("Welcome Zonion  !! :)");
        message.setText("your otp is: " + otp);

        mailSender.send(message);

        otpStorage.put(normalizedEmail, new OtpDetails(otp, System.currentTimeMillis()));
        System.out.println("==== OTP Storage Debug Info ====");
        System.out.println("Email: " + email);
        System.out.println("Generated OTP: " + otp);
        System.out.println("Storage size: " + otpStorage.size());
        System.out.println("Current storage contents:");
        otpStorage.forEach((k, v) -> {
            System.out.println("  Email: " + k);
            System.out.println("  OTP: " + v.getOtp());
            System.out.println("  Timestamp: " + new Date(v.getTimeStamp()));
            System.out.println("  ---------------");
        });
        System.out.println("==============================");
    }

    public boolean verifyOtp(String userOtp, String email) {

        userOtp = userOtp.trim();

        String normalizedEmail = email.toLowerCase();
        OtpDetails storedOtp = otpStorage.get(normalizedEmail);

        System.out.println("\n==== OTP Verification Debug Info ====");
        System.out.println("Attempting to verify OTP for email: " + email);
        System.out.println("User provided OTP: " + userOtp);
        System.out.println("Current storage size: " + otpStorage.size());

        // Debug print current storage state
        System.out.println("\nCurrent storage contents:");
        otpStorage.forEach((k, v) -> {
            System.out.println("  Email: " + k);
            System.out.println("  Stored OTP: " + v.getOtp());
            System.out.println("  Timestamp: " + new Date(v.getTimeStamp()));
            System.out.println("  ---------------");
        });

        // Get stored OTP details with null check
//        OtpDetails storedOtp = otpStorage.get(email);
        System.out.println("\nRetrieved OTP details for " + email + ": " +
                (storedOtp != null ? storedOtp.toString() : "null"));

        // Early return if no OTP found
        if (storedOtp == null) {
            System.out.println("No OTP found for email - verification failed");
            System.out.println("=====================================\n");
            return false;
        }

        // Check expiration
        long now = System.currentTimeMillis();
        long timeDifference = now - storedOtp.getTimeStamp();
        System.out.println("Time since OTP generation: " + (timeDifference / 1000) + " seconds");

        if (timeDifference > OTP_DURATION) {
            System.out.println("OTP expired - verification failed");
            otpStorage.remove(email);
            System.out.println("=====================================\n");
            return false;
        }

        // Verify OTP
        boolean isValid = storedOtp.getOtp().equals(userOtp.trim());
        System.out.println("OTP verification result: " + (isValid ? "valid" : "invalid"));

        // Remove OTP if valid
        if (isValid) {
            otpStorage.remove(email);
            System.out.println("OTP removed from storage after successful verification");
        }

        System.out.println("=====================================\n");
        return isValid;
    }

//    public void sendPasswordResetOTP(String otp, String email) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("muhammedelham@gmail.com");
//        message.setTo(email);
//        message.setSubject("Password Reset Request");
//        message.setText("Your password reset OTP is: " + otp);
//        mailSender.send(message);
//
//        otpStorage.put("reset-" + email, new OtpDetails(otp, System.currentTimeMillis()));
//        System.out.println("==== OTP Storage Debug Info ====");
//        System.out.println(otpStorage.toString());
//    }

    public void sendPasswordResetOTP(String otp, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("muhammedelham@gmail.com");
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Your password reset OTP is: " + otp);
        mailSender.send(message);

        // Store with consistent key format
        otpStorage.put(email, new OtpDetails(otp, System.currentTimeMillis()));

        System.out.println("==== Password Reset OTP Storage Debug Info ====");
        System.out.println("Email: " + email);
        System.out.println("Generated OTP: " + otp);
        System.out.println("Storage size: " + otpStorage.size());
    }

//    public boolean verifyPasswordResetOTP(String userOtp, String email) {
//        OtpDetails storedOtp = otpStorage.get("reset-" + email);
//        if (storedOtp == null) return false;
//
//        long now = System.currentTimeMillis();
//        if (now - storedOtp.getTimeStamp() > OTP_DURATION) {
//            otpStorage.remove("reset-" + email);
//            return false;
//        }
//
//        boolean isValid = storedOtp.getOtp().equals(userOtp);
//        if (isValid) {
//            otpStorage.remove("reset-" + email);
//        }
//        return isValid;
//
//    }

    public boolean verifyPasswordResetOTP(String userOtp, String email) {
        System.out.println("\n==== Password Reset OTP Verification Debug Info ====");
        System.out.println("Attempting to verify OTP for email: " + email);
        System.out.println("User provided OTP: " + userOtp);

        OtpDetails storedOtp = otpStorage.get(email);
        if (storedOtp == null) {
            System.out.println("No OTP found for email - verification failed");
            return false;
        }

        long now = System.currentTimeMillis();
        long timeDifference = now - storedOtp.getTimeStamp();
        System.out.println("Time since OTP generation: " + (timeDifference / 1000) + " seconds");

        if (timeDifference > OTP_DURATION) {
            System.out.println("OTP expired - verification failed");
            otpStorage.remove(email);
            return false;
        }

        boolean isValid = storedOtp.getOtp().equals(userOtp);
        if (isValid) {
            otpStorage.remove(email);
            System.out.println("OTP verified successfully and removed from storage");
        } else {
            System.out.println("Invalid OTP provided");
        }

        return isValid;
    }
}

class OtpDetails {
    private String otp;
    private long timeStamp;

    public OtpDetails(String otp, long timeStamp) {
        this.otp = otp;
        this.timeStamp = timeStamp;
    }

    public String getOtp() {
        return otp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
