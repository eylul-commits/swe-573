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
                
                if (userCount != null && userCount > 0) {
                    log.info("✓ Database already initialized with {} users. Skipping data loader.", userCount);
                } else {
                    log.info("✓ Database is empty. Initial data should be loaded from data.sql");
                }
            } catch (Exception e) {
                log.error("Failed to check database initialization: {}", e.getMessage(), e);
            }
        };
    }
}

