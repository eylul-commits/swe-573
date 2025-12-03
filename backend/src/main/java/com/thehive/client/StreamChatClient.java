package com.thehive.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class StreamChatClient {

    @Value("${stream.chat.api-key:}")
    private String apiKey;

    @Value("${stream.chat.secret:}")
    private String apiSecret;

    private final RestTemplate restTemplate;
    private static final String STREAM_API_BASE_URL = "https://chat.stream-io-api.com";

    public StreamChatClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    // No-arg constructor for tests
    public StreamChatClient() {
        this.restTemplate = new RestTemplate();
    }


    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty();
    }

    private String generateServerToken() {
        try {
            // Create header
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

            // Create payload with server role
            String payload = "{\"server\":true}";
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            // Create signature
            String dataToSign = header + "." + encodedPayload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] signature = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature);

            return header + "." + encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate server token", e);
        }
    }

    private HttpHeaders createAuthenticatedHeaders() {
        // Generate server token
        String serverToken = generateServerToken();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", serverToken);
        headers.set("Stream-Auth-Type", "jwt");
        
        return headers;
    }

    public void upsertUser(Integer userId, String name) {
        if (!isConfigured()) {
            log.warn("Stream Chat not configured. Skipping user upsert for user ID: {}", userId);
            return;
        }

        Map<Integer, String> users = new HashMap<>();
        users.put(userId, name);
        upsertUsers(users);
    }

    public void upsertUsers(Map<Integer, String> usersMap) {
        if (!isConfigured()) {
            log.warn("Stream Chat not configured. Skipping batch user upsert.");
            return;
        }

        try {
            // Build users object for API call
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> users = new HashMap<>();
            
            for (Map.Entry<Integer, String> entry : usersMap.entrySet()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", entry.getKey().toString());
                userData.put("name", entry.getValue());
                userData.put("role", "user");
                users.put(entry.getKey().toString(), userData);
            }
            
            requestBody.put("users", users);

            // Prepare authenticated headers
            HttpHeaders headers = createAuthenticatedHeaders();

            // Make API call
            String url = String.format("%s/users?api_key=%s", STREAM_API_BASE_URL, apiKey);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                log.info("✓ Batch upserted {} users to Stream Chat", usersMap.size());
            } else {
                log.error("✗ Failed to upsert users. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("✗ Failed to batch upsert users to Stream Chat: {}", e.getMessage(), e);
        }
    }

    public void deleteAllUsers() {
        if (!isConfigured()) {
            log.warn("Stream Chat not configured. Skipping user deletion.");
            return;
        }

        try {
            // Prepare authenticated headers
            HttpHeaders headers = createAuthenticatedHeaders();

            // Delete all users (hard delete)
            String url = String.format("%s/users?api_key=%s&delete_type=hard", STREAM_API_BASE_URL, apiKey);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(new HashMap<>(), headers);
            
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            
            log.info("✓ Deleted all users from Stream Chat");
        } catch (Exception e) {
            log.error("✗ Failed to delete all users from Stream Chat: {}", e.getMessage(), e);
        }
    }

    public void deleteAllChannels() {
        if (!isConfigured()) {
            log.warn("Stream Chat not configured. Skipping channel deletion.");
            return;
        }

        try {
            // Prepare authenticated headers
            HttpHeaders headers = createAuthenticatedHeaders();

            // Query all channels
            Map<String, Object> queryBody = new HashMap<>();
            queryBody.put("filter_conditions", new HashMap<>());
            queryBody.put("limit", 100);
            
            String queryUrl = String.format("%s/channels?api_key=%s", STREAM_API_BASE_URL, apiKey);
            HttpEntity<Map<String, Object>> queryRequest = new HttpEntity<>(queryBody, headers);
            
            ResponseEntity<Map> queryResponse = restTemplate.postForEntity(queryUrl, queryRequest, Map.class);
            
            if (queryResponse.getBody() != null && queryResponse.getBody().containsKey("channels")) {
                List<Map<String, Object>> channels = (List<Map<String, Object>>) queryResponse.getBody().get("channels");
                
                if (channels.isEmpty()) {
                    log.info("No channels to delete from Stream Chat");
                    return;
                }

                // Delete each channel
                int deletedCount = 0;
                for (Map<String, Object> channel : channels) {
                    try {
                        String channelType = (String) channel.get("type");
                        String channelId = (String) channel.get("id");
                        
                        if (channelType != null && channelId != null) {
                            String deleteUrl = String.format("%s/channels/%s/%s?api_key=%s&hard_delete=true", 
                                STREAM_API_BASE_URL, channelType, channelId, apiKey);
                            
                            HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
                            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteRequest, String.class); // post'taki gibi olmuyor
                            //custom header kullanmaya izin veren tek delete metodu bu

                            deletedCount++;
                        }
                    } catch (Exception e) {
                        log.warn("Failed to delete channel: {}", e.getMessage());
                    }
                }
                
                log.info("✓ Deleted {} channels from Stream Chat", deletedCount);
            }
        } catch (Exception e) {
            log.error("✗ Failed to delete channels from Stream Chat: {}", e.getMessage(), e);
        }
    }

    public void clearAllData() {
        if (!isConfigured()) {
            log.warn("Stream Chat not configured. Skipping data clear.");
            return;
        }

        log.info("Clearing all Stream Chat data...");
        deleteAllChannels();
        deleteAllUsers();
        log.info("✓ Stream Chat data cleared");
    }
}

