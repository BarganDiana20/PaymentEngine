package com.paymentengine.server.service.impl;

import org.springframework.stereotype.Service;
import com.paymentengine.server.model.User;
import com.paymentengine.server.repository.UserRepository;
import com.paymentengine.server.service.UserService;
import com.paymentengine.server.security.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
         
        //we only do password hashing in Service
        String hashedPassword = passwordEncoder.hash(user.getPassword());
        user.setPasswordHash(hashedPassword);
        
        return userRepository.insert(user);

    }

    @Override 
    public User login(String usernameOrEmail, String rawPassword){
        Optional<User> userOpt =
                usernameOrEmail.contains("@")
                ? userRepository.findByEmail(usernameOrEmail.toLowerCase())
                : userRepository.findByUsername(usernameOrEmail);

        User user = userOpt.orElseThrow(
        () -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
               throw new IllegalArgumentException("Invalid credentials");
        }

        return user;

    }

    public void deleteUser(int id){
        
        userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        userRepository.delete(id);
    }

    public User findById(int id){
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    

}
