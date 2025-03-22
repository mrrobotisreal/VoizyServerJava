package io.winapps.voizy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.posts.GetPostMediaResponse;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.util.JsonUtil;
import io.winapps.voizy.util.TestDatabaseUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
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

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetPostMediaIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(GetPostMediaIntegrationTest.class);
    private static final String BASE_URL = "http://localhost:8282";
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private long postId = 0;

    @BeforeAll
    public void setup() throws Exception {
        System.setProperty("TEST_MODE", "true");

        TestDatabaseUtil.setupTestDatabase();

        TestDatabaseUtil.cleanupTestData();
    }

    @AfterAll
    public void cleanup() throws Exception {
        TestDatabaseUtil.cleanupTestData();
        logger.info("Test cleanup complete");
    }

    @Test
    public void testGetPostMedia() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testGetPostMedia with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/get/media?id=" + postId;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(200, response.getCode());

                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Response body: {}", responseBody);

                GetPostMediaResponse getResponse = objectMapper.readValue(responseBody, GetPostMediaResponse.class);

                assertNotNull(getResponse);
                assertNotNull(getResponse.getImages());
                assertNotNull(getResponse.getVideos());

                assertTrue(getResponse.getImages().get(0).startsWith("Test media url"));
                assertTrue(getResponse.getVideos().get(0).startsWith("Test media url"));
            }
        }
    }

    @Test
    public void testGetPostMediaInvalidParameters() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testGetPostMediaInvalidParameters with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/get/media?id=invalid";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(400, response.getCode());
            }
        }
    }

    @Test
    public void testGetPostMediaUnauthorized() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testGetPostMediaUnauthorized with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/get/media?id=" + postId;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(401, response.getCode());
            }
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", "invalid-api-key");
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(401, response.getCode());
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

    private void insertTestPosts(long userId, String testId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String insertPostQuery = "INSERT INTO posts (" +
                    "user_id, to_user_id, content_text, created_at, updated_at" +
                    ") VALUES (?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();

            try (PreparedStatement postStmt = conn.prepareStatement(insertPostQuery)) {
                postStmt.setLong(1, userId);
                postStmt.setLong(2, -1);
                postStmt.setString(3, "Test post for " + testId);
                postStmt.setTimestamp(4, Timestamp.valueOf(now.minusHours(7)));
                postStmt.setTimestamp(5, Timestamp.valueOf(now.minusHours(7)));

                postStmt.executeUpdate();

                logger.info("Inserted test post for user ID: {}", userId);
            }

            String selectPostQuery = "SELECT post_id FROM posts WHERE user_id = ? LIMIT 1";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectPostQuery)) {
                selectStmt.setLong(1, userId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        postId = rs.getLong("post_id");
                    }
                }
            }

            String insertMediaQuery = "INSERT INTO post_media (" +
                    "post_id, media_url, media_type, uploaded_at) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement mediaStmt = conn.prepareStatement(insertMediaQuery)) {
                for (int i = 0; i < 7; i++) {
                    if (postId == 0) {
                        throw new SQLException("Failed to get postId");
                    }
                    mediaStmt.setLong(1, postId);
                    mediaStmt.setString(2, "Test media url " + (i + 1) + " for test " + testId);
                    mediaStmt.setString(3, (i >= 3 && i % 2 != 0) ? "video" : "image");
                    mediaStmt.setTimestamp(4, Timestamp.valueOf(now.minusHours(i)));

                    mediaStmt.executeUpdate();
                }

                logger.info("Inserted 7 media urls to post {} for test {}", postId, testId);
            }
        }
    }
}
