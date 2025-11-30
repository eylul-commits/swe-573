package com.thehive.config;

import com.thehive.client.StreamChatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final JdbcTemplate jdbcTemplate;
    private final StreamChatClient streamChatClient;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            try {
                log.info("Clearing Stream Chat data on startup...");
                clearStreamChatData();

                // Check if users exist
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                
                if (userCount != null && userCount > 0) {
                    log.info("✓ Database already initialized with {} users.", userCount);
                    
                    // Sync all users to Stream Chat
                    
                    //Warning: this is a hack to wait for the Stream Chat data to be cleared, bad practice :(
                    Thread.sleep(10000);
                    syncUsersToStreamChat();
                } else {
                    log.info("✓ Database is empty. Initial data should be loaded from data.sql");
                    
                    // Clear Stream Chat data when database is reset
                    clearStreamChatData();
                }
            } catch (Exception e) {
                log.error("Failed to check database initialization: {}", e.getMessage(), e);
            }
        };
    }

    private void syncUsersToStreamChat() {
        try {
            if (!streamChatClient.isConfigured()) {
                log.warn("Stream Chat not configured. Skipping user sync.");
                return;
            }

            // Query all users from database
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, name FROM users"
            );

            if (users.isEmpty()) {
                log.info("No users to sync to Stream Chat");
                return;
            }

            // Build user map for batch upsert
            Map<Integer, String> userMap = new HashMap<>();
            for (Map<String, Object> user : users) {
                Integer id = (Integer) user.get("id");
                String name = (String) user.get("name");
                userMap.put(id, name);
            }

            // Batch upsert to Stream Chat
            streamChatClient.upsertUsers(userMap);
            
            log.info("✓ Synced {} users to Stream Chat", userMap.size());
        } catch (Exception e) {
            log.error("✗ Failed to sync users to Stream Chat: {}", e.getMessage(), e);
        }
    }

    private void clearStreamChatData() {
        try {
            if (!streamChatClient.isConfigured()) {
                log.warn("Stream Chat not configured. Skipping data clear.");
                return;
            }

            log.info("Database reset detected. Clearing Stream Chat data...");
            streamChatClient.clearAllData();
        } catch (Exception e) {
            log.error("✗ Failed to clear Stream Chat data: {}", e.getMessage(), e);
        }
    }
}

