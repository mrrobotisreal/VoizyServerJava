package io.winapps.voizy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.CreatePostResponse;
import io.winapps.voizy.services.PostService;
import io.winapps.voizy.util.JsonUtil;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostControllerCreatePostTest {
    @Mock
    private PostService postService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    private PostController postController;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws IOException {
        postController = new PostController(postService);
        objectMapper = JsonUtil.getObjectMapper();

        when(request.getMethod()).thenReturn("POST");

        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        CreatePostRequest postRequest = createSamplePostRequest();
        String requestBody = objectMapper.writeValueAsString(postRequest);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(inputStream));

        CreatePostResponse mockResponse = new CreatePostResponse();
        mockResponse.setSuccess(true);
        mockResponse.setMessage("Post created successfully");
        mockResponse.setPostId(123L);

        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(mockResponse);

        postController.createPost(request, response);

        verify(response).setContentType("application/json");
        verify(postService).createPost(any(CreatePostRequest.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void testCreatePost_InvalidMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET"); // Wrong method

        postController.createPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
        verify(postService, never()).createPost(any(CreatePostRequest.class));
    }

    @Test
    public void testCreatePost_InvalidUserId() throws Exception {
        CreatePostRequest postRequest = createSamplePostRequest();
        postRequest.setUserId(0); // Invalid user ID

        String requestBody = objectMapper.writeValueAsString(postRequest);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(inputStream));

        postController.createPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid userID");
        verify(postService, never()).createPost(any(CreatePostRequest.class));
    }

    @Test
    public void testCreatePost_ServiceError() throws Exception {
        CreatePostRequest postRequest = createSamplePostRequest();
        String requestBody = objectMapper.writeValueAsString(postRequest);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(inputStream));

        when(postService.createPost(any(CreatePostRequest.class)))
                .thenThrow(new Exception("Service error"));

        postController.createPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error creating post"));
    }

    @Test
    public void testCreatePost_InvalidJson() throws Exception {
        String invalidJson = "{invalid:json}";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(inputStream));

        postController.createPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error creating post"));
        verify(postService, never()).createPost(any(CreatePostRequest.class));
    }

    private CreatePostRequest createSamplePostRequest() {
        CreatePostRequest request = new CreatePostRequest();
        request.setUserId(1L);
        request.setToUserId(-1L);
        request.setContentText("This is a test post");
        request.setLocationName("Test Location");
        request.setLocationLat(40.7128);
        request.setLocationLong(-74.0060);
        request.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
        request.setHashtags(Arrays.asList("test", "example"));
        request.setPoll(false);
        return request;
    }

    private static class ServletInputStreamMock extends jakarta.servlet.ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public ServletInputStreamMock(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {
            // Not implemented for this mock
        }
    }
}
