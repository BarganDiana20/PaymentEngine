package com.paymentengine.server.repository.session;

import org.jdbi.v3.core.Jdbi;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnection {
    
    private static volatile DatabaseConnection INSTANCE;
    private final Jdbi jdbi;

    private DatabaseConnection() {
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("database.properties not found in classpath");
            }

            Properties properties = new Properties();
            properties.load(input);

            String url = properties.getProperty("jdbc.url");
            String username = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            if (username == null || password == null) {
                throw new RuntimeException(
                        "Environment variables DB_USER and DB_PASSWORD must be set"
                );
            }

            this.jdbi = Jdbi.create(url, username, password);

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public static DatabaseConnection getInstance() {
       
        if (INSTANCE == null) {
            synchronized (DatabaseConnection.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseConnection();
                }
            }
        }
        return INSTANCE;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }
}
