package io.winapps.voizy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.CreatePostResponse;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.util.JsonUtil;
import io.winapps.voizy.util.TestDatabaseUtil;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreatePostIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CreatePostIntegrationTest.class);
    private static final String BASE_URL = "http://localhost:8282";
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @BeforeAll
    public void setup() throws Exception {
        System.setProperty("TEST_MODE", "true");

        TestDatabaseUtil.setupTestDatabase();

        TestDatabaseUtil.cleanupTestData();

        logger.info("Test setup complete");
    }

    @AfterAll
    public void cleanup() throws Exception {
        TestDatabaseUtil.cleanupTestData();
        logger.info("Test cleanup complete");
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "create_post_test_" + testId + "@example.com";
        String testUsername = "test_create_post_" + testId;

        logger.info("Running testCreatePost_Success with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();
        String token = userResponse.getToken();

        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setUserId(userId);
        postRequest.setToUserId(-1L);
        postRequest.setContentText("Test post created by integration test " + testId);
        postRequest.setLocationName("Test Location");
        postRequest.setLocationLat(40.7128);
        postRequest.setLocationLong(-74.0060);
        postRequest.setImages(Collections.singletonList("test_image_" + testId + ".jpg"));
        postRequest.setHashtags(Arrays.asList("test", "integration", testId));
        postRequest.setPoll(false);

        CreatePostResponse postResponse = createPost(postRequest, apiKey, token);

        assertNotNull(postResponse);
        assertTrue(postResponse.isSuccess());
        assertEquals("Post created successfully", postResponse.getMessage());
        assertNotNull(postResponse.getPostId());

        boolean postExists = verifyPostExists(postResponse.getPostId(), userId);
        assertTrue(postExists, "Post should exist in database");
    }

    @Test
    public void testCreatePoll_Success() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "create_poll_test_" + testId + "@example.com";
        String testUsername = "test_create_poll_" + testId;

        logger.info("Running testCreatePoll_Success with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Poll Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();
        String token = userResponse.getToken();

        CreatePostRequest pollRequest = new CreatePostRequest();
        pollRequest.setUserId(userId);
        pollRequest.setToUserId(-1L);
        pollRequest.setContentText("Test poll created by integration test " + testId);
        pollRequest.setLocationName("Test Location");
        pollRequest.setLocationLat(40.7128);
        pollRequest.setLocationLong(-74.0060);
        pollRequest.setPoll(true);
        pollRequest.setPollQuestion("What is your favorite color?");
        pollRequest.setPollDurationType("days");
        pollRequest.setPollDurationLength(3);
        pollRequest.setPollOptions(Arrays.asList("Red", "Blue", "Green", "Yellow"));

        CreatePostResponse pollResponse = createPost(pollRequest, apiKey, token);

        assertNotNull(pollResponse);
        assertTrue(pollResponse.isSuccess());
        assertEquals("Post created successfully", pollResponse.getMessage());
        assertNotNull(pollResponse.getPostId());

        boolean pollExists = verifyPollExists(pollResponse.getPostId(), userId);
        assertTrue(pollExists, "Poll should exist in database");

        int optionsCount = countPollOptions(pollResponse.getPostId());
        assertEquals(4, optionsCount, "Poll should have 4 options");
    }

    @Test
    public void testCreatePost_Unauthorized() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "unauth_test_" + testId + "@example.com";
        String testUsername = "test_unauth_" + testId;

        logger.info("Running testCreatePost_Unauthorized with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Unauth Test " + testId);
        long userId = userResponse.getUserID();

        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setUserId(userId);
        postRequest.setToUserId(-1L);
        postRequest.setContentText("Test post that should be unauthorized");
        postRequest.setLocationName("Test Location");
        postRequest.setLocationLat(40.7128);
        postRequest.setLocationLong(-74.0060);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/posts/create");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(postRequest),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(401, response.getCode(), "Should return 401 Unauthorized");
            }
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/posts/create");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(postRequest),
                    ContentType.APPLICATION_JSON));

            httpPost.setHeader("Authorization", "Bearer invalid-token");
            httpPost.setHeader("X-API-Key", "invalid-api-key");
            httpPost.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(401, response.getCode(), "Should return 401 Unauthorized");
            }
        }
    }

    private CreateUserResponse createTestUser(String email, String username, String preferredName) throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setUsername(username);
        request.setPassword("password123");
        request.setPreferredName(preferredName);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/users/create");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(request),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(200, response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                CreateUserResponse userResponse = objectMapper.readValue(responseBody, CreateUserResponse.class);

                logger.info("Created test user with ID: {} and email: {}", userResponse.getUserID(), email);
                return userResponse;
            }
        }
    }

    private CreatePostResponse createPost(CreatePostRequest request, String apiKey, String token) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/posts/create");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(request),
                    ContentType.APPLICATION_JSON));

            httpPost.setHeader("Authorization", "Bearer " + token);
            httpPost.setHeader("X-API-Key", apiKey);
            httpPost.setHeader("X-User-ID", String.valueOf(request.getUserId()));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(200, response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Create post response: {}", responseBody);
                return objectMapper.readValue(responseBody, CreatePostResponse.class);
            }
        }
    }

    private boolean verifyPostExists(long postId, long userId) throws SQLException {
        String query = "SELECT post_id FROM posts WHERE post_id = ? AND user_id = ?";

        try (Connection conn = io.winapps.voizy.database.DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, postId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean verifyPollExists(long postId, long userId) throws SQLException {
        String query = "SELECT post_id FROM posts WHERE post_id = ? AND user_id = ? AND is_poll = 1";

        try (Connection conn = io.winapps.voizy.database.DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, postId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int countPollOptions(long postId) throws SQLException {
        String query = "SELECT COUNT(*) FROM poll_options WHERE post_id = ?";

        try (Connection conn = io.winapps.voizy.database.DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }
}
