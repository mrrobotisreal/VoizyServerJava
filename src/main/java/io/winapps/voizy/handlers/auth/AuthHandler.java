package io.winapps.voizy.handlers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.auth.LoginRequest;
import io.winapps.voizy.models.auth.LoginResponse;
import io.winapps.voizy.models.auth.User;
import io.winapps.voizy.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    /**
     * Handle login requests
     */
    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("POST")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            LoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), LoginRequest.class);

            if (loginRequest.getEmail() == null && loginRequest.getUsername() == null) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Missing required body params. Either 'email' or 'username' must be provided.");
                return;
            }

            LoginResponse loginResponse = doLogin(loginRequest);

            if (!loginResponse.isPasswordCorrect()) {
                logger.warn("INVALID PASSWORD ATTEMPTED for username: {}, email: {}",
                        loginRequest.getUsername(), loginRequest.getEmail());
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Email, Username, or Password is incorrect.");
                return;
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("email", loginRequest.getEmail());
            metadata.put("username", loginRequest.getUsername());
            AnalyticsUtil.trackEvent(loginResponse.getUserID(), "login", "user", loginResponse.getUserID(), metadata);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), loginResponse);

        } catch (Exception e) {
            logger.error("Error processing login request", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error logging in: " + e.getMessage());
        }
    }

    /**
     * Process login request
     */
    private LoginResponse doLogin(LoginRequest req) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            User user = fetchUser(conn, req);

            if (user == null) {
                throw new Exception("User not found");
            }

            boolean isPasswordCorrect = HashingUtil.checkPasswordHash(
                    req.getPassword() + user.getSalt(),
                    user.getPasswordHash()
            );

            String token = null;
            if (isPasswordCorrect) {
                token = JwtUtil.generateAndStoreJWT(String.valueOf(user.getUserID()), "always");
            }

            LoginResponse response = new LoginResponse();
            response.setPasswordCorrect(isPasswordCorrect);
            response.setUserID(user.getUserID());
            response.setApiKey(user.getApiKey());
            response.setToken(token);
            response.setEmail(user.getEmail());
            response.setUsername(user.getUsername());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());

            return response;

        } catch (SQLException e) {
            logger.error("Database error during login", e);
            throw new Exception("Database error during login: " + e.getMessage());
        }
    }

    /**
     * Fetch user from database by email or username
     */
    private User fetchUser(Connection conn, LoginRequest req) throws SQLException {
        String query;
        String param;

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            query = "SELECT user_id, email, username, password_hash, salt, api_key, created_at, updated_at " +
                    "FROM users WHERE email = ? LIMIT 1";
            param = req.getEmail();
        } else {
            query = "SELECT user_id, email, username, password_hash, salt, api_key, created_at, updated_at " +
                    "FROM users WHERE username = ? LIMIT 1";
            param = req.getUsername();
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserID(rs.getLong("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setSalt(rs.getString("salt"));
                    user.setApiKey(rs.getString("api_key"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return user;
                }
            }
        }

        return null;
    }
}
