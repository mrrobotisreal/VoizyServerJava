package io.winapps.voizy.controllers;

import io.winapps.voizy.models.posts.GetPostMediaResponse;
import io.winapps.voizy.services.PostService;
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
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostControllerGetPostMediaTest {
    @Mock
    private PostService postService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    private PostController postController;

    @BeforeEach
    public void setup() throws IOException {
        postController = new PostController(postService);

        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void testGetPostMedia_Success() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("123");

        GetPostMediaResponse mockResponse = new GetPostMediaResponse();
        mockResponse.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
        mockResponse.setVideos(Collections.emptyList());

        when(postService.getPostMedia(123L)).thenReturn(mockResponse);

        postController.getPostMedia(request, response);

        verify(response).setContentType("application/json");
        verify(postService).getPostMedia(123L);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void testGetPostMedia_InvalidMethod() throws Exception {
        when(request.getMethod()).thenReturn("POST");

        postController.getPostMedia(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
        verify(postService, never()).getPostMedia(anyLong());
    }

    @Test
    public void testGetPostMedia_MissingId() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn(null);

        postController.getPostMedia(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'id'");
        verify(postService, never()).getPostMedia(anyLong());
    }

    @Test
    public void testGetPostMedia_InvalidId() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("not-a-number");

        postController.getPostMedia(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid post ID"));
        verify(postService, never()).getPostMedia(anyLong());
    }

    @Test
    public void testGetPostMedia_ServiceException() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("123");

        when(postService.getPostMedia(123L)).thenThrow(new RuntimeException("Unexpected service error"));

        postController.getPostMedia(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error getting post media"));
    }
}
