package com.paymentengine.server.repository.impl;

import org.springframework.stereotype.Repository;
import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Role;
import com.paymentengine.server.repository.UserRepository;
import com.paymentengine.server.repository.session.DatabaseConnection;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Jdbi jdbi;
    
    public UserRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public User insert(User user) {
        int generatedId = jdbi.withHandle(
            handle -> handle.createUpdate("INSERT INTO users (first_name, last_name, username, email, password_hash, role) VALUES (:firstName, :lastName, :username, :email, :passwordHash, :role)")
                  .bind("firstName", user.getFirstName())
                  .bind("lastName", user.getLastName())
                  .bind("username", user.getUsername())
                  .bind("email", user.getEmail())
                  .bind("passwordHash", user.getPasswordHash())
                  .bind("role", user.getRole().name())
                  .executeAndReturnGeneratedKeys("id")
                  .mapTo(int.class)
                  .one()
        );

        user.setId(generatedId);
        return user;
    }

    @Override
    public void delete(int id) {
        jdbi.useHandle(handle ->
            handle.createUpdate("DELETE FROM users WHERE id = :id")
                  .bind("id", id)
                  .execute()
        );

    }

    @Override
    public Optional<User> findById(int id) {
        
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE id = :id")
                  .bind("id", id)
                  .mapToBean(User.class)
                  .findFirst()
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE username = :username")
                  .bind("username", username)
                  .mapToBean(User.class)
                  .findFirst()
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
      
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE email = :email")
                  .bind("email", email)
                  .mapToBean(User.class)
                  .findFirst()
        );
    }


}
