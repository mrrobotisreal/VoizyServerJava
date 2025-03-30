package io.winapps.voizy.services;

import io.winapps.voizy.models.users.GetUserProfileResponse;
import io.winapps.voizy.repositories.ProfileRepository;
import io.winapps.voizy.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceGetUserProfileTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;

    private UserService userService;

    @BeforeEach
    public void setup() { userService = new UserService(userRepository, profileRepository); }

    @Test
    public void testGetUserProfile_success() throws Exception {
        long userId = 123L;
        long profileId = 125L;
        String username = "testUser";
        String preferredName = "Big T";
        String firstName = "Test";
        String lastName = "User";
        LocalDate birthDate = LocalDateTime.now().toLocalDate();
        String cityOfResidence = "Seattle";
        String placeOfWork = "WinApps.io";
        LocalDateTime dateJoined = LocalDateTime.now();

        GetUserProfileResponse mockResponse = new GetUserProfileResponse();
        mockResponse.setUsername(username);
        mockResponse.setProfileID(profileId);
        mockResponse.setUserID(userId);
        mockResponse.setPreferredName(preferredName);
        mockResponse.setFirstName(firstName);
        mockResponse.setLastName(lastName);
        mockResponse.setBirthDate(birthDate);
        mockResponse.setCityOfResidence(cityOfResidence);
        mockResponse.setPlaceOfWork(placeOfWork);
        mockResponse.setDateJoined(dateJoined);

        when(userRepository.getProfile(userId)).thenReturn(mockResponse);

        GetUserProfileResponse response = userService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals(profileId, response.getProfileID());
        assertEquals(userId, response.getUserID());
        assertEquals(preferredName, response.getPreferredName());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
        assertEquals(birthDate, response.getBirthDate());
        assertEquals(cityOfResidence, response.getCityOfResidence());
        assertEquals(placeOfWork, response.getPlaceOfWork());
        assertEquals(dateJoined, response.getDateJoined());

        verify(userRepository).getProfile(userId);
    }

    @Test
    public void testGetUserProfile_DatabaseError() throws Exception {
        long userId = 456L;

        when(userRepository.getProfile(userId)).thenThrow(new SQLException("Database Error"));

        GetUserProfileResponse response = userService.getUserProfile(userId);

        assertNotNull(response);

        verify(userRepository).getProfile(userId);
    }
}
