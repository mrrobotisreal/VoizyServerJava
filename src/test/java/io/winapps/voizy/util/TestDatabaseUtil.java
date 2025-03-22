package io.winapps.voizy.util;

import io.winapps.voizy.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseUtil.class);

    private static final String TEST_EMAIL_DOMAIN = "example.com";
    private static final String TEST_USERNAME_PREFIX = "test_";

    public static void setupTestDatabase() throws SQLException {
        System.setProperty("TEST_MODE", "true");

        if (!isDatabaseInitialized()) {
            DatabaseManager.initMySQL();
        }
    }

    public static void cleanupTestData() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM posts WHERE user_id IN (" +
                            "  SELECT user_id FROM users WHERE email LIKE ?)" +
                            "OR content_text LIKE 'Test post%'")) {
                stmt.setString(1, "%@" + TEST_EMAIL_DOMAIN);
                int deleted = stmt.executeUpdate();
                logger.info("Deleted {} test posts", deleted);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM user_profiles WHERE user_id IN (" +
                            "  SELECT user_id FROM users WHERE email LIKE ?)")) {
                stmt.setString(1, "%@" + TEST_EMAIL_DOMAIN);
                int deleted = stmt.executeUpdate();
                logger.info("Deleted {} test user profiles", deleted);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM api_keys WHERE user_id IN (" +
                            "  SELECT user_id FROM users WHERE email LIKE ?)")) {
                stmt.setString(1, "%@" + TEST_EMAIL_DOMAIN);
                int deleted = stmt.executeUpdate();
                logger.info("Deleted {} test API keys", deleted);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM users WHERE email LIKE ? OR username LIKE ?")) {
                stmt.setString(1, "%@" + TEST_EMAIL_DOMAIN);
                stmt.setString(2, TEST_USERNAME_PREFIX + "%");
                int deleted = stmt.executeUpdate();
                logger.info("Deleted {} test users", deleted);
            }
        }
    }

    public static boolean testUsersExist() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE email LIKE ?")) {
            stmt.setString(1, "%@" + TEST_EMAIL_DOMAIN);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static boolean isDatabaseInitialized() {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
