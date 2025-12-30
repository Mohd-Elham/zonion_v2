package com.example.demo.service;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;// check this deep seek

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {// check this deep seek
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public void setNewPassword(String email, String password) {
        Users newUser = userRepository.findByEmail(email);
        newUser.setPassword(encoder.encode(password));
        userRepository.save(newUser);


    }

    public void setNewPasswordd(String email, String newPassword) {
        Users user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        }
    }



//    This is to register users
    public void registerUser(String username, String password, String email, String phone) {
        Users newUser = new Users();
        String randomUUID = UUID.randomUUID().toString();

        newUser.setId(randomUUID);

        newUser.setUsername(username);

        String encodedPassword = encoder.encode(password); // check this deep seek
        System.out.println("Encoded password during registration: " + encodedPassword);
        newUser.setPassword(encodedPassword);

        newUser.setEmail(email);

        newUser.setPhone(phone);

        newUser.setActive(true);

        newUser.setRoles("ROLE_USER");

        newUser.setCreated_at(System.currentTimeMillis());


        System.out.println("The new registered user after otp is " + newUser.toString());


        userRepository.save(newUser);


    }


    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid id"));
    }

    public void updateStatus(Users user) {
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public Page<Users> getAllUserss(PageRequest of) {
        return userRepository.findAll(of);
    }

    public Users findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<Users> getUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }

    public Users getIdByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Users findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
