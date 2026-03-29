package com.paymentengine.server.service;

import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Transaction;

import java.util.List;

public interface UserService {

    User registerUser(User user);

    User login(String usernameOrEmail, String passwordHash);

    void deleteUser(int id);

    User findById(int id);
    
}

