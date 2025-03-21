package io.winapps.voizy.repositories;

import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class ProfileRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProfileRepository.class);

    public Profile create(long userId, CreateUserRequest request) throws SQLException {
        String query = "INSERT INTO user_profiles (user_id, first_name, last_name, preferred_name, " +
                "birth_date, city_of_residence, place_of_work, date_joined) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseManager.getConnection();
            LocalDateTime currentTime = LocalDateTime.now();

            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, userId);
            stmt.setString(2, request.getPreferredName());
            stmt.setNull(3, Types.VARCHAR);
            stmt.setString(4, request.getPreferredName());
            stmt.setNull(5, Types.DATE);
            stmt.setNull(6, Types.VARCHAR);
            stmt.setNull(7, Types.VARCHAR);
            stmt.setTimestamp(8, Timestamp.valueOf(currentTime));

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Failed to insert profile, affected rows: " + affected);
            }

            generatedKeys = stmt.getGeneratedKeys();
            long profileId;
            if (generatedKeys.next()) {
                profileId = generatedKeys.getLong(1);
            } else {
                throw new SQLException("Failed to get profile ID after insert");
            }

            // Create and return profile object
            Profile profile = new Profile();
            profile.setProfileID(profileId);
            profile.setUserID(userId);
            profile.setFirstName(request.getPreferredName());
            profile.setPreferredName(request.getPreferredName());
            profile.setDateJoined(currentTime);

            return profile;

        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { logger.error("Error closing result set", e); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { logger.error("Error closing statement", e); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { logger.error("Error closing connection", e); }
        }
    }
}
