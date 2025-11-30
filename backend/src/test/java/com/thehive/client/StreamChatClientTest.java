package com.thehive.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // to automatically process mock annotations
class StreamChatClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private StreamChatClient streamChatClient;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_API_SECRET = "test-api-secret";

    @BeforeEach
    void setUp() {
        // Set up configuration values
        ReflectionTestUtils.setField(streamChatClient, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(streamChatClient, "apiSecret", TEST_API_SECRET);
    }

    @Test
    void isConfigured_whenBothApiKeyAndSecretPresent_shouldReturnTrue() {
        //No need for arrange or verify
        //Act
        boolean isConfigured =  streamChatClient.isConfigured();

        //Assert
        assertTrue(isConfigured);
    }

    @Test
    void isConfigured_whenKeyAndSecretAreMissing_shouldReturnFalse() {
        //Arrange
        StreamChatClient streamChatClientWithMissingApiKey = new StreamChatClient();

        //Act
        boolean isConfigured = streamChatClientWithMissingApiKey.isConfigured();

        //Assert
        assertFalse(isConfigured);
    }
    
    @Test
    void isConfigured_whenKeyAndSecretAreEmpty_shouldReturnFalse() {
        //Arrange
        StreamChatClient streamChatClientWithEmptyApiKey = new StreamChatClient();
        ReflectionTestUtils.setField(streamChatClientWithEmptyApiKey, "apiKey", "");
        ReflectionTestUtils.setField(streamChatClientWithEmptyApiKey, "apiSecret", "");

        //Act
        boolean isConfigured = streamChatClientWithEmptyApiKey.isConfigured();

        //Assert
        assertFalse(isConfigured);
    }

    @Test
    void createAuthenticatedHeaders_shouldReturnHeadersWithAuthorizationAndContentType() {
        // Act - invoke the private method using reflection
        HttpHeaders headers = ReflectionTestUtils.invokeMethod(streamChatClient, "createAuthenticatedHeaders"); // private diye böyle çağırıyorum

        // Assert - verify all required headers are present
        assertNotNull(headers, "Headers should not be null");
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType(), "Content-Type should be application/json");
        assertTrue(headers.containsKey("Authorization"), "Authorization header should be present");
        assertTrue(headers.containsKey("Stream-Auth-Type"), "Stream-Auth-Type header should be present");
        assertEquals("jwt", headers.getFirst("Stream-Auth-Type"), "Stream-Auth-Type should be 'jwt'");
        
        // Verify the Authorization token (header.payload.signature)
        String authToken = headers.getFirst("Authorization");
        assertNotNull(authToken, "Authorization token should not be null");
        String[] jwtParts = authToken.split("\\."); // "." ile ayrılıyor jwt formatında
        assertEquals(3, jwtParts.length, "JWT should have 3 parts (header.payload.signature)");
        assertTrue(jwtParts[0].length() > 0, "JWT header should not be empty");
        assertTrue(jwtParts[1].length() > 0, "JWT payload should not be empty");
        assertTrue(jwtParts[2].length() > 0, "JWT signature should not be empty");
    }

    @Test
    void upsertUsers_whenConfigured_shouldCallStreamApi() {
        // Arrange - create a map of users to upsert
        Map<Integer, String> usersMap = new HashMap<>();
        usersMap.put(1, "John Doe");
        usersMap.put(2, "Jane Smith");

        // Arrange - mock REST template response, simulating a Stream API response
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"users\":{}}", HttpStatus.CREATED);
        
        // Create captors to capture the method arguments
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        
        when(restTemplate.postForEntity(urlCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act - call the method
        streamChatClient.upsertUsers(usersMap);

        // Verify URL contains the correct API key parameter
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("api_key=" + TEST_API_KEY));
        assertTrue(capturedUrl.contains("https://chat.stream-io-api.com/users"));

        // Verify Request Body    
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        Map<String, Object> requestBody = (Map<String, Object>) capturedEntity.getBody();
        assertNotNull(requestBody, "Request body should not be null");
        Map<String, Object> users = (Map<String, Object>) requestBody.get("users");
        assertEquals(2, users.size(), "Should have 2 users");
        
        // Verify individual user data
        Map<String, Object> user2 = (Map<String, Object>) users.get("2");
        assertNotNull(user2, "User 2 should be present");
        assertEquals("2", user2.get("id"));
        assertEquals("Jane Smith", user2.get("name"));
        assertEquals("user", user2.get("role"));
    }

    @Test
    void upsertUsers_whenNotConfigured_shouldSkipUpsert() {
        ReflectionTestUtils.setField(streamChatClient, "apiKey", null);
        ReflectionTestUtils.setField(streamChatClient, "apiSecret", null);

        Map<Integer, String> usersMap = new HashMap<>();
        usersMap.put(1, "John Doe");

        streamChatClient.upsertUsers(usersMap);

        verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void deleteAllUsers_whenConfigured_shouldCallStreamApi() {
        // Arrange - mock REST template response
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"task_id\":\"123\"}", HttpStatus.OK);
        
        // Create captors to capture the method arguments
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        
        when(restTemplate.exchange(urlCaptor.capture(), methodCaptor.capture(), 
                httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act - call the method
        streamChatClient.deleteAllUsers();

        // Assert - verify the correct API call was made
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("api_key=" + TEST_API_KEY));
        assertTrue(capturedUrl.contains("https://chat.stream-io-api.com/users"));
        assertTrue(capturedUrl.contains("delete_type=hard"));
        
        // Verify HTTP method
        assertEquals(HttpMethod.DELETE, methodCaptor.getValue());
    }

    @Test
    void deleteAllChannels_whenConfigured_shouldQueryAndDeleteChannels() {
        // Arrange - mock query response with 2 channels (matching Stream API format)
        Map<String, Object> channel1Data = new HashMap<>();
        channel1Data.put("type", "messaging");
        channel1Data.put("id", "channel-1");
        
        Map<String, Object> channel1 = new HashMap<>();
        channel1.put("channel", channel1Data);
        
        Map<String, Object> channel2Data = new HashMap<>();
        channel2Data.put("type", "messaging");
        channel2Data.put("id", "channel-2");
        
        Map<String, Object> channel2 = new HashMap<>();
        channel2.put("channel", channel2Data);
        
        List<Map<String, Object>> channels = Arrays.asList(channel1, channel2);
        Map<String, Object> queryResponseBody = new HashMap<>();
        queryResponseBody.put("channels", channels);
        
        ResponseEntity<Map> queryResponse = new ResponseEntity<>(queryResponseBody, HttpStatus.OK);
        ResponseEntity<String> deleteResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        
        // Mock the query call
        when(restTemplate.postForEntity(contains("/channels?"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(queryResponse);
        
        // Mock the delete calls
        when(restTemplate.exchange(contains("/channels/messaging/"), eq(HttpMethod.DELETE), 
                any(HttpEntity.class), eq(String.class)))
                .thenReturn(deleteResponse);

        // Act - call the method
        streamChatClient.deleteAllChannels();

        // Assert - verify query was called once
        verify(restTemplate, times(1)).postForEntity(
                contains("/channels?"), 
                any(HttpEntity.class), 
                eq(Map.class)
        );
        
        // Verify delete was called for each channel (2 times)
        verify(restTemplate, times(2)).exchange(
                contains("/channels/messaging/"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}

