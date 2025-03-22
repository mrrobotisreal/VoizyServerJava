package io.winapps.voizy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.posts.ListPostsResponse;
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
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListPostsIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ListPostsIntegrationTest.class);
    private static final String BASE_URL = "http://localhost:8282";
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

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
    public void testListPosts() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testListPosts with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/list?id=" + userId + "&limit=10&page=1";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(200, response.getCode());

                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Response body: {}", responseBody);

                ListPostsResponse listResponse = objectMapper.readValue(responseBody, ListPostsResponse.class);

                assertNotNull(listResponse);
                assertNotNull(listResponse.getPosts());

                assertEquals(5, listResponse.getPosts().size());
                assertEquals(5, listResponse.getTotalPosts());
                assertEquals(1, listResponse.getTotalPages());
                assertEquals(10, listResponse.getLimit());
                assertEquals(1, listResponse.getPage());

                assertTrue(listResponse.getPosts().get(0).getContentText().startsWith("Test post"));
                assertEquals(userId, listResponse.getPosts().get(0).getUserId());
            }
        }
    }

    @Test
    public void testListPostsPagination() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testListPostsPagination with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/list?id=" + userId + "&limit=2&page=1";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(200, response.getCode());

                String responseBody = EntityUtils.toString(response.getEntity());
                ListPostsResponse listResponse = objectMapper.readValue(responseBody, ListPostsResponse.class);

                assertEquals(2, listResponse.getPosts().size());
                assertEquals(5, listResponse.getTotalPosts());
                assertEquals(3, listResponse.getTotalPages());  // Ceil(5/2) = 3
                assertEquals(2, listResponse.getLimit());
                assertEquals(1, listResponse.getPage());

                assertTrue(listResponse.getPosts().get(0).getContentText().contains("Test post 1"));
                assertTrue(listResponse.getPosts().get(1).getContentText().contains("Test post 2"));
            }
        }

        url = BASE_URL + "/posts/list?id=" + userId + "&limit=2&page=2";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(200, response.getCode());

                String responseBody = EntityUtils.toString(response.getEntity());
                ListPostsResponse listResponse = objectMapper.readValue(responseBody, ListPostsResponse.class);

                assertEquals(2, listResponse.getPosts().size());
                assertTrue(listResponse.getPosts().get(0).getContentText().contains("Test post 3"));
                assertTrue(listResponse.getPosts().get(1).getContentText().contains("Test post 4"));
            }
        }
    }

    @Test
    public void testListPostsInvalidParameters() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testListPostsInvalidParameters with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        insertTestPosts(userId, testId);

        String url = BASE_URL + "/posts/list?id=invalid&limit=10&page=1";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(400, response.getCode());
            }
        }

        url = BASE_URL + "/posts/list?id=" + userId;

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
    public void testListPostsUnauthorized() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "posts_test_" + testId + "@example.com";
        String testUsername = "test_posts_" + testId;

        logger.info("Running testListPostsUnauthorized with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Posts Test " + testId);
        long userId = userResponse.getUserID();

        String url = BASE_URL + "/posts/list?id=" + userId + "&limit=10&page=1";

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

            try (PreparedStatement stmt = conn.prepareStatement(insertPostQuery)) {
                // Insert 5 test posts with unique content for this test run
                for (int i = 0; i < 5; i++) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, -1);
                    stmt.setString(3, "Test post " + (i + 1) + " for " + testId);
                    stmt.setTimestamp(4, Timestamp.valueOf(now.minusHours(i)));
                    stmt.setTimestamp(5, Timestamp.valueOf(now.minusHours(i)));

                    stmt.executeUpdate();
                }

                logger.info("Inserted 5 test posts for user ID: {}", userId);
            }
        }
    }
}
