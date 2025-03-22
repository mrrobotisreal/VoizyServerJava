package io.winapps.voizy.services;

import io.winapps.voizy.models.auth.User;
import io.winapps.voizy.models.middleware.APIKey;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.models.users.Profile;
import io.winapps.voizy.repositories.ProfileRepository;
import io.winapps.voizy.repositories.UserRepository;
import io.winapps.voizy.util.AnalyticsUtil;
import io.winapps.voizy.util.ApiKeyUtil;
import io.winapps.voizy.util.HashingUtil;
import io.winapps.voizy.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserService() {
        this.userRepository = new UserRepository();
        this.profileRepository = new ProfileRepository();
    }

    public UserService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public CreateUserResponse createUser(CreateUserRequest request) throws Exception {
        try {
            String salt = HashingUtil.generateSalt(10);
            String hashedPassword = HashingUtil.hashPassword(request.getPassword() + salt);

            APIKey apiKey = ApiKeyUtil.generateSecureAPIKey();

            User user = userRepository.create(request, hashedPassword, salt, apiKey);

            userRepository.storeApiKey(user.getUserID(), apiKey);

            String token = JwtUtil.generateAndStoreJWT(String.valueOf(user.getUserID()), "always");

            Profile profile = profileRepository.create(user.getUserID(), request);

            trackUserCreationEvents(user.getUserID(), profile.getProfileID(), request);

            return buildCreateUserResponse(user, profile, token);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new Exception("Failed to create user: " + e.getMessage());
        }
    }

    private void trackUserCreationEvents(long userId, long profileId, CreateUserRequest request) {
        Map<String, Object> accountMetadata = new HashMap<>();
        accountMetadata.put("email", request.getEmail());
        accountMetadata.put("username", request.getUsername());
        AnalyticsUtil.trackEvent(userId, "create_account", "user", userId, accountMetadata);

        Map<String, Object> profileMetadata = new HashMap<>();
        profileMetadata.put("preferredName", request.getPreferredName());
        AnalyticsUtil.trackEvent(userId, "create_profile", "user_profile", profileId, profileMetadata);
    }

    private CreateUserResponse buildCreateUserResponse(User user, Profile profile, String token) {
        CreateUserResponse response = new CreateUserResponse();
        response.setUserID(user.getUserID());
        response.setProfileID(profile.getProfileID());
        response.setApiKey(user.getApiKey());
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setPreferredName(profile.getPreferredName());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setDateJoined(profile.getDateJoined());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
