package com.paymentengine.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.paymentengine.server.model.Role;
import com.paymentengine.server.model.User;
import com.paymentengine.server.model.UserResponse;
import com.paymentengine.server.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create user
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody User user) {
        user.setRole(Role.USER);
        User created = userService.registerUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(created));
    }

    //seting user in session
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @RequestParam String usernameOrEmail,
            @RequestParam String password, 
            HttpSession session) {
                
        User user = userService.login(usernameOrEmail, password);
        
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole());
        
        return ResponseEntity.ok(new UserResponse(user));
    
    }
    
    // Logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok().build();
    }


    // Get own my profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(new UserResponse(user));
    }

    //Delete User
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.deleteUser(userId);

        session.invalidate();

        return ResponseEntity.noContent().build();
    }
}