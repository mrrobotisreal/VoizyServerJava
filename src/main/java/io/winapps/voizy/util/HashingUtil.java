package io.winapps.voizy.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class HashingUtil {
    private static final Logger logger = LoggerFactory.getLogger(HashingUtil.class);

    /**
     * Generate a random salt of specified length
     * @param length Length of the salt to generate
     * @return Base64 encoded salt string
     */
    public static String generateSalt(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Hash a password using BCrypt
     * @param password The password to hash
     * @return Hashed password string
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(15));
    }

    /**
     * Check if a password matches its hash
     * @param password Plain text password
     * @param hash Hash to check against
     * @return True if the password matches the hash
     */
    public static boolean checkPasswordHash(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            logger.error("Error checking password hash", e);
            return false;
        }
    }
}
