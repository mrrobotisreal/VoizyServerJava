package io.winapps.voizy.repositories;

import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.auth.User;
import io.winapps.voizy.models.middleware.APIKey;
import io.winapps.voizy.models.users.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public User create(CreateUserRequest request, String passwordHash, String salt, APIKey apiKey) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            LocalDateTime currentTime = LocalDateTime.now();

            String userQuery = "INSERT INTO users (email, api_key, salt, password_hash, username, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, request.getEmail());
            stmt.setString(2, apiKey.getKey());
            stmt.setString(3, salt);
            stmt.setString(4, passwordHash);
            stmt.setString(5, request.getUsername());
            stmt.setTimestamp(6, Timestamp.valueOf(currentTime));
            stmt.setTimestamp(7, Timestamp.valueOf(currentTime));

            int userAffected = stmt.executeUpdate();
            if (userAffected != 1) {
                throw new SQLException("Failed to insert user, affected rows: " + userAffected);
            }

            generatedKeys = stmt.getGeneratedKeys();
            long userID;
            if (generatedKeys.next()) {
                userID = generatedKeys.getLong(1);
            } else {
                throw new SQLException("Failed to get user ID after insert");
            }

            User user = new User();
            user.setUserID(userID);
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setSalt(salt);
            user.setPasswordHash(passwordHash);
            user.setApiKey(apiKey.getKey());
            user.setCreatedAt(currentTime);
            user.setUpdatedAt(currentTime);

            conn.commit();

            return user;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { logger.error("Error closing result set", e); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { logger.error("Error closing statement", e); }
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

    public void storeApiKey(long userId, APIKey apiKey) throws SQLException {
        String query = "INSERT INTO api_keys (user_id, api_key, created_at, expires_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            stmt.setString(2, apiKey.getKey());
            stmt.setTimestamp(3, Timestamp.valueOf(apiKey.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(apiKey.getExpiresAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(apiKey.getUpdatedAt()));

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Failed to insert API key, affected rows: " + affected);
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        return findByField("email", email);
    }

    public User findByUsername(String username) throws SQLException {
        return findByField("username", username);
    }

    private User findByField(String fieldName, String value) throws SQLException {
        String query = "SELECT user_id, email, username, password_hash, salt, api_key, created_at, updated_at " +
                "FROM users WHERE " + fieldName + " = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);

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
