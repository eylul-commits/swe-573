package com.thehive.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            try {
                // Check if users exist
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                
                if (userCount == null || userCount == 0) {
                    // Create a default test user using JDBC
                    jdbcTemplate.update(
                        "INSERT INTO users (email, password_hash, name, bio, province, district, role, balance_hours) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS user_role), ?)",
                        "test@example.com",
                        "$2a$10$dummyHashForTestUser",
                        "Test User",
                        "Default test user for development",
                        "Istanbul",
                        "Beşiktaş",
                        "USER",
                        3
                    );
                    
                    log.info("✓ Created default test user");
                } else {
                    log.info("✓ Users already exist in database (count: {}). Skipping initialization.", userCount);
                }
            } catch (Exception e) {
                log.error("Failed to initialize test user: {}", e.getMessage(), e);
            }
        };
    }
}

