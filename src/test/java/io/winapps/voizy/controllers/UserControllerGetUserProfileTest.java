package io.winapps.voizy.controllers;

import io.winapps.voizy.models.users.GetUserProfileResponse;
import io.winapps.voizy.services.UserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserControllerGetUserProfileTest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    private UserController userController;

    @BeforeEach
    public void setup() throws IOException {
        userController = new UserController(userService);

        when(response.getOutputStream()).thenReturn(outputStream);
    }

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

        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("123");

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

        when(userService.getUserProfile(userId)).thenReturn(mockResponse);

        userController.getProfile(request, response);

        verify(response).setContentType("application/json");
        verify(userService).getUserProfile(userId);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void testGetUserProfile_invalidMethod() throws Exception {
        when(request.getMethod()).thenReturn("POST");

        userController.getProfile(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    public void testGetUserProfile_MissingId() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn(null);

        userController.getProfile(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required param 'id'");
        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    public void testGetUserProfile_InvalidId() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("not-a-number");

        userController.getProfile(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid user ID"));
        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    public void testGetUserProfile_ServiceException() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("123");

        when(userService.getUserProfile(123L)).thenThrow(new RuntimeException("Unexpected service error"));

        userController.getProfile(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error getting the user profile"));
    }
}
