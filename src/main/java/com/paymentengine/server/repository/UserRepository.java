package com.paymentengine.server.repository;

import com.paymentengine.server.model.User;
import java.util.Optional;

public interface UserRepository {
     
    User insert(User user);

    void delete(int id);

    Optional<User> findById(int id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
    
}
