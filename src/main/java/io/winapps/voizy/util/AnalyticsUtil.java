package io.winapps.voizy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsUtil {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Track an analytics event asynchronously
     * @param userID User ID associated with the event
     * @param eventType Type of event
     * @param objectType Type of object the event relates to
     * @param objectID ID of the object the event relates to (can be null)
     * @param metadata Additional metadata for the event (can be null)
     */
    public static void trackEvent(long userID, String eventType, String objectType, Long objectID, Map<String, Object> metadata) {
        executorService.submit(() -> {
            try {
                String metadataJson = null;
                if (metadata != null) {
                    metadataJson = objectMapper.writeValueAsString(metadata);
                }

                try (Connection conn = DatabaseManager.getConnection()) {
                    String query;
                    PreparedStatement stmt;

                    if (objectID != null) {
                        query = "INSERT INTO analytics_events (user_id, event_type, object_type, object_id, event_time, metadata) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setLong(1, userID);
                        stmt.setString(2, eventType);
                        stmt.setString(3, objectType);
                        stmt.setLong(4, objectID);
                        stmt.setObject(5, LocalDateTime.now());

                        if (metadataJson != null) {
                            stmt.setString(6, metadataJson);
                        } else {
                            stmt.setNull(6, Types.VARCHAR);
                        }
                    } else {
                        query = "INSERT INTO analytics_events (user_id, event_type, object_type, event_time, metadata) " +
                                "VALUES (?, ?, ?, ?, ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setLong(1, userID);
                        stmt.setString(2, eventType);
                        stmt.setString(3, objectType);
                        stmt.setObject(4, LocalDateTime.now());

                        if (metadataJson != null) {
                            stmt.setString(5, metadataJson);
                        } else {
                            stmt.setNull(5, Types.VARCHAR);
                        }
                    }

                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        logger.info("TrackEvent \"{}\" success; total rows affected = {}", eventType, rowsAffected);
                    } else {
                        logger.warn("TrackEvent fail; no rows affected = {}", rowsAffected);
                    }
                }
            } catch (Exception e) {
                logger.error("Error tracking event: {}", eventType, e);
            }
        });
    }

    /**
     * Shutdown the executor service
     */
    public static void shutdown() {
        executorService.shutdown();
    }
}
