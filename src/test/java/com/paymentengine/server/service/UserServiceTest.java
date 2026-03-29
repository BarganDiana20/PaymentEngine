package com.paymentengine.server.service;

import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Role;
import com.paymentengine.server.repository.UserRepository;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.security.PasswordEncoder;
import com.paymentengine.server.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_success() {
        User user = new User();
        user.setFirstName("Popescu");
        user.setLastName("George");
        user.setUsername("george_popescu");
        user.setEmail("george.popescu@gmail.com");
        user.setPassword("rawPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("george.popescu@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("george_popescu")).thenReturn(Optional.empty());
        when(passwordEncoder.hash("rawPassword")).thenReturn("hashed");
        when(userRepository.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(user);

        assertEquals("hashed", saved.getPasswordHash());
        verify(userRepository).insert(user);
    }

    @Test
    void registerUser_withEmailAlreadyExists() {
        User user = new User();
        user.setEmail("george.popescu@gmail.com");

        when(userRepository.findByEmail("george.popescu@gmail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(user));
    }

    @Test
    void login_withUsernameAndCorrectPassword() {
        User user = new User();
        user.setUsername("george_popescu");
        user.setPasswordHash("hashed");

        when(userRepository.findByUsername("george_popescu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

        User logged = userService.login("george_popescu", "raw");

        assertNotNull(logged);
    }

    @Test
    void login_withEmailAndCorrectPassword() {
        User user = new User();
        user.setEmail("georgepopescu@gmail.com");
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail("georgepopescu@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

        User logged = userService.login("georgepopescu@gmail.com", "raw");

        assertNotNull(logged);
        assertEquals("georgepopescu@gmail.com", logged.getEmail());
    }

    @Test
    void login_withUsernameAndWrongPassword() {
        User user = new User();
        user.setUsername("george_popescu");
        user.setPasswordHash("hashed");

        when(userRepository.findByUsername("george_popescu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("george_popescu", "wrong"));
    }

     @Test
    void login_withEmailAndWrongPassword() {
        User user = new User();
        user.setEmail( "georgepopescu@gmail.com");
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail("georgepopescu@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("georgepopescu@gmail.com", "wrong"));
    }

    @Test
    void login_withUsername_andUserNotFound() {
        when(userRepository.findByUsername("george_popescu")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("george_popescu", "pass"));
    }
    
    @Test
    void login_withEmail_andUserNotFound() {
        when(userRepository.findByEmail("georgepopescu@gmail.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("georgepopescu@gmail.com", "pass"));
    }

    @Test
    void whenDeleteUser() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.USER);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        userService.deleteUser(1);
        verify(userRepository).delete(1);
    }

    @Test
    void whenFindByCorrectId() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.USER);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = userService.findById(1);

        assertNotNull(result);
    }

    @Test
    void whenFindByIncorrectID() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.USER);

        when(userRepository.findById(1)).thenReturn(Optional.empty());
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(1);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
}
