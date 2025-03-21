package io.winapps.voizy.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.middleware.APIKey;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.util.AnalyticsUtil;
import io.winapps.voizy.util.ApiKeyUtil;
import io.winapps.voizy.util.HashingUtil;
import io.winapps.voizy.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Placeholder implementations of handler methods
     * These would be fully implemented with database operations
     */
    public void getUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getUser not yet implemented\"}");
    }

    public void updateUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"updateUser not yet implemented\"}");
    }

    public void getProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getProfile not yet implemented\"}");
    }

    public void listUserProfiles(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"listUserProfiles not yet implemented\"}");
    }

    public void updateUserProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"updateUserProfile not yet implemented\"}");
    }

    public void listSongs(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"listSongs not yet implemented\"}");
    }

    public void getTotalImages(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getTotalImages not yet implemented\"}");
    }

    public void getProfilePic(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getProfilePic not yet implemented\"}");
    }

    public void getCoverPic(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getCoverPic not yet implemented\"}");
    }

    public void listImages(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"listImages not yet implemented\"}");
    }

    public void updateProfilePic(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"updateProfilePic not yet implemented\"}");
    }

    public void updateCoverPic(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"updateCoverPic not yet implemented\"}");
    }

    public void putUserImages(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"putUserImages not yet implemented\"}");
    }

    public void getBatchUserImagesPresignedPutUrls(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getBatchUserImagesPresignedPutUrls not yet implemented\"}");
    }

    public void createFriendRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"createFriendRequest not yet implemented\"}");
    }

    public void listFriendships(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"listFriendships not yet implemented\"}");
    }

    public void listFriendsInCommon(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"listFriendsInCommon not yet implemented\"}");
    }

    public void getTotalFriends(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: finish implementation...
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{\"message\": \"getTotalFriends not yet implemented\"}");
    }

    /**
     * Implementation of user creation
     */
    private CreateUserResponse doCreateUser(CreateUserRequest req) throws Exception {
        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement apiKeyStmt = null;
        PreparedStatement profileStmt = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String salt = HashingUtil.generateSalt(10);
            String hashedPassword = HashingUtil.hashPassword(req.getPassword() + salt);

            APIKey apiKey = ApiKeyUtil.generateSecureAPIKey();

            LocalDateTime currentTime = LocalDateTime.now();

            String userQuery = "INSERT INTO users (email, api_key, salt, password_hash, username, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, req.getEmail());
            userStmt.setString(2, apiKey.getKey());
            userStmt.setString(3, salt);
            userStmt.setString(4, hashedPassword);
            userStmt.setString(5, req.getUsername());
            userStmt.setTimestamp(6, Timestamp.valueOf(currentTime));
            userStmt.setTimestamp(7, Timestamp.valueOf(currentTime));

            int userAffected = userStmt.executeUpdate();
            if (userAffected != 1) {
                throw new SQLException("Failed to insert user, affected rows: " + userAffected);
            }

            ResultSet userKeys = userStmt.getGeneratedKeys();
            long userID;
            if (userKeys.next()) {
                userID = userKeys.getLong(1);
            } else {
                throw new SQLException("Failed to get user ID after insert");
            }

            LocalDateTime expiresAt = currentTime.plusDays(90);

            String apiKeyQuery = "INSERT INTO api_keys (user_id, api_key, created_at, expires_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

            apiKeyStmt = conn.prepareStatement(apiKeyQuery);
            apiKeyStmt.setLong(1, userID);
            apiKeyStmt.setString(2, apiKey.getKey());
            apiKeyStmt.setTimestamp(3, Timestamp.valueOf(currentTime));
            apiKeyStmt.setTimestamp(4, Timestamp.valueOf(expiresAt));
            apiKeyStmt.setTimestamp(5, Timestamp.valueOf(currentTime));

            int apiKeyAffected = apiKeyStmt.executeUpdate();
            if (apiKeyAffected != 1) {
                throw new SQLException("Failed to insert API key, affected rows: " + apiKeyAffected);
            }

            String token = JwtUtil.generateAndStoreJWT(String.valueOf(userID), "always");

            String profileQuery = "INSERT INTO user_profiles (user_id, first_name, last_name, preferred_name, " +
                    "birth_date, city_of_residence, place_of_work, date_joined) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            profileStmt = conn.prepareStatement(profileQuery, Statement.RETURN_GENERATED_KEYS);
            profileStmt.setLong(1, userID);
            profileStmt.setString(2, req.getPreferredName());
            profileStmt.setNull(3, Types.VARCHAR);
            profileStmt.setString(4, req.getPreferredName());
            profileStmt.setNull(5, Types.DATE);
            profileStmt.setNull(6, Types.VARCHAR);
            profileStmt.setNull(7, Types.VARCHAR);
            profileStmt.setTimestamp(8, Timestamp.valueOf(currentTime));

            int profileAffected = profileStmt.executeUpdate();
            if (profileAffected != 1) {
                throw new SQLException("Failed to insert user profile, affected rows: " + profileAffected);
            }

            ResultSet profileKeys = profileStmt.getGeneratedKeys();
            long profileID;
            if (profileKeys.next()) {
                profileID = profileKeys.getLong(1);
            } else {
                throw new SQLException("Failed to get profile ID after insert");
            }

            conn.commit();

            CreateUserResponse response = new CreateUserResponse();
            response.setUserID(userID);
            response.setProfileID(profileID);
            response.setApiKey(apiKey.getKey());
            response.setToken(token);
            response.setEmail(req.getEmail());
            response.setUsername(req.getUsername());
            response.setPreferredName(req.getPreferredName());
            response.setFirstName(req.getPreferredName());
            response.setDateJoined(currentTime);
            response.setCreatedAt(currentTime);
            response.setUpdatedAt(currentTime);

            return response;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                }
            }
            logger.error("Error creating user", e);
            throw new Exception("Failed to create user: " + e.getMessage());
        } finally {
            if (profileStmt != null) try {
                profileStmt.close();
            } catch (SQLException e) {
                logger.error("Error closing statement", e);
            }
            if (apiKeyStmt != null) try {
                apiKeyStmt.close();
            } catch (SQLException e) {
                logger.error("Error closing statement", e);
            }
            if (userStmt != null) try {
                userStmt.close();
            } catch (SQLException e) {
                logger.error("Error closing statement", e);
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    /**
     * Handle create user requests
     */
    public void createUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("POST")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            CreateUserRequest createRequest = objectMapper.readValue(req.getInputStream(), CreateUserRequest.class);

            CreateUserResponse response = doCreateUser(createRequest);

            Map<String, Object> accountMetadata = new HashMap<>();
            accountMetadata.put("email", response.getEmail());
            accountMetadata.put("username", response.getUsername());
            AnalyticsUtil.trackEvent(response.getUserID(), "create_account", "user", response.getUserID(), accountMetadata);

            Map<String, Object> profileMetadata = new HashMap<>();
            profileMetadata.put("preferredName", response.getPreferredName());
            AnalyticsUtil.trackEvent(response.getUserID(), "create_profile", "user_profile", response.getProfileID(), profileMetadata);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), response);

        } catch (Exception e) {
            logger.error("Error creating user", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating the user: " + e.getMessage());
        }
    }
}
