package io.winapps.voizy.services;

import io.winapps.voizy.models.auth.LoginRequest;
import io.winapps.voizy.models.auth.LoginResponse;
import io.winapps.voizy.models.auth.User;
import io.winapps.voizy.models.middleware.APIKey;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.models.users.Profile;
import io.winapps.voizy.repositories.ProfileRepository;
import io.winapps.voizy.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    private UserService userService;
    private AuthService authService;

    @BeforeEach
    public void setup() {
        userService = new UserService(userRepository, profileRepository);
        authService = new AuthService(userRepository);
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setPreferredName("Test User");

        User mockUser = new User();
        mockUser.setUserID(1L);
        mockUser.setEmail(request.getEmail());
        mockUser.setUsername(request.getUsername());
        mockUser.setSalt("salt");
        mockUser.setPasswordHash("hashedPassword");
        mockUser.setApiKey("apikey123");
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());

        Profile mockProfile = new Profile();
        mockProfile.setProfileID(1L);
        mockProfile.setUserID(1L);
        mockProfile.setPreferredName(request.getPreferredName());
        mockProfile.setFirstName(request.getPreferredName());
        mockProfile.setDateJoined(LocalDateTime.now());

        when(userRepository.create(eq(request), anyString(), anyString(), any(APIKey.class)))
                .thenReturn(mockUser);
        when(profileRepository.create(eq(1L), eq(request)))
                .thenReturn(mockProfile);

        CreateUserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserID());
        assertEquals(1L, response.getProfileID());
        assertEquals("apikey123", response.getApiKey());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getPreferredName());

        verify(userRepository).create(eq(request), anyString(), anyString(), any(APIKey.class));
        verify(userRepository).storeApiKey(eq(1L), any(APIKey.class));
        verify(profileRepository).create(eq(1L), eq(request));
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User mockUser = new User();
        mockUser.setUserID(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setSalt("salt");
        mockUser.setPasswordHash("$2a$15$abcdefghijklmnopqrstuvwxyz012345678901234567890");
        mockUser.setApiKey("apikey123");
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserID());
        assertEquals("apikey123", response.getApiKey());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("testuser", response.getUsername());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testLogin_InvalidUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("nonexistentuser")).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(request);
        });

        assertEquals("Login failed: User not found", exception.getMessage());

        verify(userRepository).findByUsername("nonexistentuser");
    }
}
