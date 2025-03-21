package io.winapps.voizy.services;

import io.winapps.voizy.models.auth.LoginRequest;
import io.winapps.voizy.models.auth.LoginResponse;
import io.winapps.voizy.models.auth.User;
import io.winapps.voizy.repositories.UserRepository;
import io.winapps.voizy.util.AnalyticsUtil;
import io.winapps.voizy.util.HashingUtil;
import io.winapps.voizy.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) throws Exception {
        try {
            User user = findUserByEmailOrUsername(request);

            if (user == null) {
                throw new Exception("User not found");
            }

            boolean isPasswordCorrect = HashingUtil.checkPasswordHash(
                    request.getPassword() + user.getSalt(),
                    user.getPasswordHash()
            );

            String token = null;
            if (isPasswordCorrect) {
                token = JwtUtil.generateAndStoreJWT(String.valueOf(user.getUserID()), "always");

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("email", user.getEmail());
                metadata.put("username", user.getUsername());
                AnalyticsUtil.trackEvent(user.getUserID(), "login", "user", user.getUserID(), metadata);
            }

            return buildLoginResponse(user, isPasswordCorrect, token);

        } catch (Exception e) {
            logger.error("Error during login", e);
            throw new Exception("Login failed: " + e.getMessage());
        }
    }

    private User findUserByEmailOrUsername(LoginRequest request) throws SQLException {
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            return userRepository.findByEmail(request.getEmail());
        } else if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            return userRepository.findByUsername(request.getUsername());
        }
        return null;
    }

    private LoginResponse buildLoginResponse(User user, boolean isPasswordCorrect, String token) {
        LoginResponse response = new LoginResponse();
        response.setPasswordCorrect(isPasswordCorrect);
        response.setUserID(user.getUserID());
        response.setApiKey(user.getApiKey());
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
