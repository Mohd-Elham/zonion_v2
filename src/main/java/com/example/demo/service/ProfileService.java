package com.example.demo.service;

import com.example.demo.models.Address;
import com.example.demo.models.Users;
import com.example.demo.repository.MyAddressRepo;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
    private final MyAddressRepo myAddressRepo;
    private final PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Autowired
    public ProfileService(UserRepository userRepository, MyAddressRepo myAddressRepo, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.myAddressRepo = myAddressRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Users getUsersById(String id) {
        return userRepository.findById(id).orElse(null);

    }


    public Users getUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateUsernameEmailPhone(String id, String username, String email, String phone) {
        Optional<Users> exsistingUser = userRepository.findById(id);

        if (exsistingUser.isPresent()) {
            Users user = exsistingUser.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            user.setActive(true);
            userRepository.save(user);
        }
    }

    public List<Address> getAddressById(String userId) {
        return myAddressRepo.findByUserId(userId);
    }

    public void addAddress(Address address, Users userId) {
        address.setUserId(userId.getId());
        myAddressRepo.save(address);
    }

    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void resetPassword(String email, String newPassword) {
        Users user = userRepository.findByEmail(email);
        if (user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword); // Hash the new password
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
    }
}
