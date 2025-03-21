package io.winapps.voizy.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.middleware.APIKey;
import io.winapps.voizy.models.middleware.ErrorResponse;
import io.winapps.voizy.util.ApiKeyUtil;
import io.winapps.voizy.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

public class AuthMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String USER_ID_CONTEXT_KEY = "userID";
    public static final String API_KEY_CONTEXT_KEY = "apiKey";

    /**
     * Middleware to validate JWT tokens
     */
    public BiConsumer<HttpServletRequest, HttpServletResponse> validateJwt(BiConsumer<HttpServletRequest, HttpServletResponse> next) {
        return (req, res) -> {
            try {
                String authHeader = req.getHeader("Authorization");
                if (authHeader == null || authHeader.isEmpty()) {
                    sendError(res, "Authorization header missing", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String[] splitToken = authHeader.split("Bearer ");
                if (splitToken.length != 2) {
                    sendError(res, "Invalid authorization format", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String tokenStr = splitToken[1];

                try {
                    Claims claims = JwtUtil.validateToken(tokenStr);
                    String userID = claims.get("userID", String.class);

                    if (userID == null) {
                        sendError(res, "Invalid token claims", HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    req.setAttribute(USER_ID_CONTEXT_KEY, userID);

                    next.accept(req, res);

                } catch (ExpiredJwtException e) {
                    sendError(res, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
                } catch (JwtException e) {
                    sendError(res, "Invalid token: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                }

            } catch (Exception e) {
                logger.error("Error validating JWT", e);
                try {
                    sendError(res, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    /**
     * Middleware to validate API keys
     */
    public BiConsumer<HttpServletRequest, HttpServletResponse> validateApiKey(BiConsumer<HttpServletRequest, HttpServletResponse> next) {
        return (req, res) -> {
            try {
                String xApiKey = req.getHeader("X-API-Key");
                if (xApiKey == null || xApiKey.isEmpty()) {
                    sendError(res, "X-API-Key missing", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String xUserIDString = req.getHeader("X-User-ID");
                if (xUserIDString == null || xUserIDString.isEmpty()) {
                    sendError(res, "X-User-ID missing", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                long xUserID;
                try {
                    xUserID = Long.parseLong(xUserIDString);
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse X-User-ID", e);
                    sendError(res, "Failed to parse X-User-ID", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                APIKey apiKey = fetchApiKey(xUserID, xApiKey);
                if (apiKey == null) {
                    sendError(res, "Failed to find API key", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                if (!ApiKeyUtil.validateAPIKey(apiKey)) {
                    sendError(res, "API key validation failed", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                if (!ApiKeyUtil.getLimiter(apiKey.getKey()).tryAcquire()) {
                    sendError(res, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS_429);
                    return;
                }

                req.setAttribute(USER_ID_CONTEXT_KEY, xUserID);
                req.setAttribute(API_KEY_CONTEXT_KEY, apiKey);

                next.accept(req, res);

            } catch (Exception e) {
                logger.error("Error validating API key", e);
                try {
                    sendError(res, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    /**
     * Combined JWT and API key middleware
     */
    public BiConsumer<HttpServletRequest, HttpServletResponse> combinedAuth(BiConsumer<HttpServletRequest, HttpServletResponse> next) {
        return validateJwt(validateApiKey(next));
    }

    /**
     * Send an error response
     */
    private void sendError(HttpServletResponse res, String message, int statusCode) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                Integer.toString(statusCode),
                message
        );

        res.setStatus(statusCode);
        res.setContentType("application/json");
        objectMapper.writeValue(res.getOutputStream(), errorResponse);
    }

    /**
     * Fetch API key from database
     */
    private APIKey fetchApiKey(long userID, String apiKeyStr) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String query = "SELECT user_id, api_key, created_at, expires_at, last_used_at, updated_at " +
                    "FROM api_keys WHERE user_id = ? AND api_key = ? LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setLong(1, userID);
                stmt.setString(2, apiKeyStr);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        APIKey apiKey = new APIKey();
                        apiKey.setKey(rs.getString("api_key"));
                        apiKey.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        apiKey.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                        apiKey.setLastUsedAt(rs.getTimestamp("last_used_at").toLocalDateTime());
                        apiKey.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        // Also validate against users table
                        if (validateUserAPIKey(conn, userID, apiKeyStr)) {
                            return apiKey;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error fetching API key", e);
        }

        return null;
    }

    /**
     * Validate API key against users table
     */
    private boolean validateUserAPIKey(Connection conn, long userID, String apiKeyStr) throws SQLException {
        String query = "SELECT user_id, api_key FROM users WHERE user_id = ? AND api_key = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userID);
            stmt.setString(2, apiKeyStr);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Update API key last used timestamp
     */
    private void updateApiKeyLastUsedAt(long userID, String apiKeyStr) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String query = "UPDATE api_keys SET last_used_at = NOW() WHERE user_id = ? AND api_key = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setLong(1, userID);
                stmt.setString(2, apiKeyStr);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected != 1) {
                    logger.warn("Incorrect rows affected: {}", rowsAffected);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error updating API key last used timestamp", e);
        }
    }
}
