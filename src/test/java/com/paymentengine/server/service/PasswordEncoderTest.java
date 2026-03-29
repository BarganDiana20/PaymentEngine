package com.paymentengine.server.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.paymentengine.server.security.PasswordEncoder;

public class PasswordEncoderTest {

    PasswordEncoder encoder = new PasswordEncoder();

    //test to verify that the same password produces different hashes
    @Test
    void hash_shouldGenerateDifferentHashesForSamePassword(){
        String raw = "password";
        String has1 = encoder.hash(raw);
        String has2 = encoder.hash(raw);

        assertNotEquals(has1, has2);
    }

    //test to verify that the password entered at login is correctly validated against the hash in the database
    @Test
    void matches_shouldReturnTrueForCorrectPassword(){
        String raw = "password";
        String has = encoder.hash(raw);

        assertTrue(encoder.matches(raw, has));
    }

    //test to verify that a wrong password is not validated
    @Test
    void matches_shouldReturnFalseForWrongPassword(){
        String has = encoder.hash("password");
        assertFalse(encoder.matches("wrong", has));
    }

    //in this test we verify that the password is not stored plain
    @Test
    void hash_shouldNotReturnPlainPassword(){
        String raw = "password";
        String has = encoder.hash(raw);

        assertNotEquals(raw, has);
    }
}
