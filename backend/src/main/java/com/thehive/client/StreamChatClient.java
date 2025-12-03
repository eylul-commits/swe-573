package com.thehive.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URI; 

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URLEncoder; 

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

            // Query all users first
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("limit", 100);

            String payload = URLEncoder.encode("{\"filter_conditions\":{}}", StandardCharsets.UTF_8);
            
            String queryUrl = String.format(
                "%s/users?payload=%s&api_key=%s",
                STREAM_API_BASE_URL,
                payload,
                apiKey
            );

            RequestEntity<Void> request = RequestEntity
                .get(URI.create(queryUrl))
                .header("Authorization", generateServerToken())
                .header("Stream-Auth-Type", "jwt")
                .build();

            ResponseEntity<Map> queryResponse = restTemplate.exchange(request, Map.class);
            
            if (queryResponse.getBody() != null && queryResponse.getBody().containsKey("users")) {
                List<Map<String, Object>> users = (List<Map<String, Object>>) queryResponse.getBody().get("users");
                
                if (users.isEmpty()) {
                    log.info("No users to delete from Stream Chat");
                    return;
                }
                
                log.info("Found {} users to delete from Stream Chat", users.size());

                // Collect all user IDs
                List<String> userIds = new ArrayList<>();
                for (Map<String, Object> user : users) {
                    String userId = (String) user.get("id");
                    if (userId != null) {
                        userIds.add(userId);
                    }
                }
                
                // Delete all users in a single batch request
                if (!userIds.isEmpty()) {
                    try {
                        String deleteUrl = String.format("%s/users/delete?api_key=%s", STREAM_API_BASE_URL, apiKey);

                        Map<String, Object> deleteBody = new HashMap<>();
                        deleteBody.put("conversations", "hard");
                        deleteBody.put("messages", "hard");
                        deleteBody.put("user", "hard");
                        deleteBody.put("user_ids", userIds);
                        
                        HttpEntity<Map<String, Object>> deleteRequest = new HttpEntity<>(deleteBody, headers);
                        
                        restTemplate.exchange(deleteUrl, HttpMethod.POST, deleteRequest, String.class);
                    } catch (Exception e) {
                        log.error("Failed to delete users: {}", e.getMessage());
                    }
                }
            }
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
                        Map<String, Object> channelData = (Map<String, Object>) channel.get("channel");
                        if (channelData == null) {
                            log.warn("Channel object is missing 'channel' wrapper: {}", channel);
                            continue;
                        }

                        String channelType = (String) channelData.get("type");
                        String channelId = (String) channelData.get("id");

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

