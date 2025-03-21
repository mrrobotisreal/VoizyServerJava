package io.winapps.voizy.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String JWT_SECRET_KEY = "my_super_duper_uber_secret_and_cool_jwt_key_for_voizy"; // TODO: replace in production; for testing only!
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes());

    public static String getJwtSecretKey() {
        return JWT_SECRET_KEY;
    }

    /**
     * Generate and store a JWT token
     * @param userID User ID to include in the token
     * @param sessionOption Session duration option ("always", "daily", "weekly", "monthly", "never")
     * @return Generated JWT token
     * @throws Exception if token generation fails
     */
    public static String generateAndStoreJWT(String userID, String sessionOption) throws Exception {
        LocalDateTime expirationTime;

        switch (sessionOption) {
            case "always":
                expirationTime = LocalDateTime.now().plusDays(366);
                break;
            case "daily":
                expirationTime = LocalDateTime.now().plusHours(24);
                break;
            case "weekly":
                expirationTime = LocalDateTime.now().plusDays(7);
                break;
            case "monthly":
                expirationTime = LocalDateTime.now().plusDays(30);
                break;
            case "never":
                expirationTime = LocalDateTime.now().plusMinutes(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid session option: " + sessionOption);
        }

        Date expDate = Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());
        Date issuedAt = new Date();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userID", userID);

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(issuedAt)
                    .setExpiration(expDate)
                    .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                    .compact();

            // TODO: store tokens in DB for validation, revocation, etc...
            return token;
        } catch (Exception e) {
            logger.error("Error generating JWT token", e);
            throw new Exception("Failed to generate JWT token: " + e.getMessage());
        }
    }

    /**
     * Validate a JWT token and extract claims
     * @param token JWT token to validate
     * @return Claims from the token if valid
     */
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
