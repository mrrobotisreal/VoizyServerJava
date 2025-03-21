package io.winapps.voizy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.auth.LoginRequest;
import io.winapps.voizy.models.auth.LoginResponse;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserApiIntegrationTest {
    private static final String BASE_URL = "http://localhost:8282";
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    static {
        System.setProperty("TEST_MODE", "true");
    }

    @BeforeAll
    public void setup() throws Exception {
        TestDatabaseUtil.setupTestDatabase();
    }

    @Test
    public void testCreateUserAndLogin() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("integration@example.com");
        createRequest.setUsername("integrationuser");
        createRequest.setPassword("integration123");
        createRequest.setPreferredName("Integration User");

        CreateUserResponse createResponse = createUser(createRequest);

        assertNotNull(createResponse);
        assertNotNull(createResponse.getUserID());
        assertEquals("integration@example.com", createResponse.getEmail());
        assertEquals("integrationuser", createResponse.getUsername());
        assertNotNull(createResponse.getApiKey());
        assertNotNull(createResponse.getToken());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("integration123");

        LoginResponse loginResponse = login(loginRequest);

        assertNotNull(loginResponse);
        assertTrue(loginResponse.isPasswordCorrect());
        assertEquals(createResponse.getUserID(), loginResponse.getUserID());
        assertEquals(createResponse.getApiKey(), loginResponse.getApiKey());
        assertNotNull(loginResponse.getToken());
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("badlogin@example.com");
        createRequest.setUsername("badloginuser");
        createRequest.setPassword("correctpassword");
        createRequest.setPreferredName("Bad Login User");

        CreateUserResponse createResponse = createUser(createRequest);
        assertNotNull(createResponse);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("badloginuser");
        loginRequest.setPassword("wrongpassword");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/users/login");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(loginRequest),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(401, response.getCode());
            }
        }
    }

    private CreateUserResponse createUser(CreateUserRequest request) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/users/create");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(request),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(200, response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, CreateUserResponse.class);
            }
        }
    }

    private LoginResponse login(LoginRequest request) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/users/login");
            httpPost.setEntity(new StringEntity(
                    objectMapper.writeValueAsString(request),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                assertEquals(200, response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, LoginResponse.class);
            }
        }
    }
}
