package io.winapps.voizy.util;

import com.google.common.util.concurrent.RateLimiter;
import io.winapps.voizy.models.middleware.APIKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiKeyUtil {
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyUtil.class);
    private static final int API_KEY_LENGTH = 32;
    private static final int MAX_REQUEST_RATE = 100;
    private static final int KEY_ROTATION_DAYS = 90;

    private static final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * Get or create a rate limiter for the given API key
     * @param apiKey API key string
     * @return RateLimiter for the API key
     */
    public static RateLimiter getLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, k -> RateLimiter.create(MAX_REQUEST_RATE));
    }

    /**
     * Get hex formatted string from bytes
     * @param bytes byte array to be converted
     * @return String converted from byte array
     */
    public static String getConvertedHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    /**
     * Generate a secure API key
     * @return APIKey object containing the key and metadata
     * @throws Exception if key generation fails
     */
    public static APIKey generateSecureAPIKey() throws Exception {
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[API_KEY_LENGTH];
            random.nextBytes(bytes);

            String key = "sk_" + getConvertedHexString(bytes);

            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expiresAt = currentTime.plusDays(KEY_ROTATION_DAYS);

            return new APIKey(
                    key,
                    currentTime,
                    currentTime,
                    expiresAt,
                    currentTime
            );
        } catch (Exception e) {
            logger.error("Error generating secure API key", e);
            throw new Exception("Failed to generate API key: " + e.getMessage());
        }
    }

    /**
     * Validate an API key
     * @param apiKey APIKey object to validate
     * @return True if the API key is valid
     */
    public static boolean validateAPIKey(APIKey apiKey) {
        if (apiKey == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(apiKey.getExpiresAt())) {
            return false;
        }

        apiKey.setLastUsedAt(LocalDateTime.now());

        return true;
    }

    /**
     * Check if key rotation is needed
     * @param apiKey APIKey object to check
     * @return True if key rotation is needed
     */
    public static boolean isKeyRotationNeeded(APIKey apiKey) {
        return LocalDateTime.now().isAfter(
                apiKey.getCreatedAt().plusDays(KEY_ROTATION_DAYS)
        );
    }
}
