package io.winapps.voizy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.users.GetUserProfileResponse;
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
public class GetUserProfileIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(GetUserProfileIntegrationTest.class);
    private static final String BASE_URL = "http://localhost:8282";
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private long userId = 0;

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
    public void testGetUserProfile() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "users_test_" + testId + "@example.com";
        String testUsername = "test_users_" + testId;

        logger.info("Running testGetUserProfile with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Users Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        updateProfile(userId, "Users Test " + testId, "Biggs", "Seattle", "WinApps.io", testId);

        String url = BASE_URL + "/users/profile/get?id=" + userId;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("X-API-Key", apiKey);
            httpGet.setHeader("X-User-ID", String.valueOf(userId));

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                assertEquals(200, response.getCode());

                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Response body: {}", responseBody);

                GetUserProfileResponse getResponse = objectMapper.readValue(responseBody, GetUserProfileResponse.class);

                assertNotNull(getResponse);
                assertEquals(userId, getResponse.getProfileID());
                assertEquals(userId, getResponse.getUserID());
                assertEquals("Users Test " + testId, getResponse.getPreferredName());
                assertEquals("Users Test " + testId, getResponse.getFirstName());
                assertEquals("Biggs", getResponse.getLastName());
                assertEquals("Seattle", getResponse.getCityOfResidence());
                assertEquals("WinApps.io", getResponse.getPlaceOfWork());
                assertNotNull(getResponse.getBirthDate());
                assertNotNull(getResponse.getDateJoined());
            }
        }
    }

    @Test
    public void testGetPostMediaInvalidParameters() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "users_test_" + testId + "@example.com";
        String testUsername = "test_users_" + testId;

        logger.info("Running testGetUserProfileInvalidParameters with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Users Test " + testId);
        long userId = userResponse.getUserID();
        String apiKey = userResponse.getApiKey();

        updateProfile(userId, "Users Test " + testId, "Biggs", "Seattle", "WinApps.io", testId);

        String url = BASE_URL + "/users/profile/get?id=invalid";

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
        String testEmail = "users_test_" + testId + "@example.com";
        String testUsername = "test_users_" + testId;

        logger.info("Running testGetUserProfileUnauthorized with ID: {}", testId);

        CreateUserResponse userResponse = createTestUser(testEmail, testUsername, "Users Test " + testId);
        long userId = userResponse.getUserID();

        updateProfile(userId, "Users Test " + testId, "Biggs", "Seattle", "WinApps.io", testId);

        String url = BASE_URL + "/users/profile/get?id=" + userId;

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

    private void updateProfile(long userId, String firstName, String lastName, String cityOfResidence, String placeOfWork, String testId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String updateProfileQuery = "UPDATE user_profiles SET profile_id = ?, user_id = ?, preferred_name = ?, first_name = ?, last_name = ?, birth_date = ?, " +
                    "city_of_residence = ?, place_of_work = ?, date_joined = ? WHERE user_id = ?;";

            try (PreparedStatement profileStmt = conn.prepareStatement(updateProfileQuery)) {
                profileStmt.setLong(1, userId);
                profileStmt.setLong(2, userId);
                profileStmt.setString(3, firstName);
                profileStmt.setString(4, firstName);
                profileStmt.setString(5, lastName);
                profileStmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                profileStmt.setString(7, cityOfResidence);
                profileStmt.setString(8, placeOfWork);
                profileStmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                profileStmt.setLong(10, userId);

                profileStmt.executeUpdate();

                logger.info("Updated profile data for user ID: {}", userId);
            }
        }
    }
}
