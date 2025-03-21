package io.winapps.voizy.util;

import io.winapps.voizy.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseUtil.class);

    public static void setupTestDatabase() throws SQLException {
        System.setProperty("TEST_MODE", "true");
        DatabaseManager.initMySQL();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE user_profiles");
            stmt.execute("TRUNCATE TABLE api_keys");
            stmt.execute("TRUNCATE TABLE analytics_events");

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
