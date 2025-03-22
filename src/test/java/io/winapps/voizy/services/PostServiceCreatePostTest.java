package io.winapps.voizy.services;

import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.CreatePostResponse;
import io.winapps.voizy.repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class PostServiceCreatePostTest {
    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    public void setup() {
        postService = new PostService(postRepository);
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        CreatePostRequest request = createSamplePostRequest();
        long expectedPostId = 123L;

        when(postRepository.createPost(any(CreatePostRequest.class))).thenReturn(expectedPostId);

        CreatePostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Post created successfully", response.getMessage());
        assertEquals(expectedPostId, response.getPostId());

        verify(postRepository).createPost(request);
    }

    @Test
    public void testCreatePost_WithPoll_Success() throws Exception {
        CreatePostRequest request = createSamplePollRequest();
        long expectedPostId = 456L;

        when(postRepository.createPost(any(CreatePostRequest.class))).thenReturn(expectedPostId);

        CreatePostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Post created successfully", response.getMessage());
        assertEquals(expectedPostId, response.getPostId());

        verify(postRepository).createPost(request);
    }

    @Test
    public void testCreatePost_WithShare_Success() throws Exception {
        CreatePostRequest request = createSampleShareRequest();
        long expectedPostId = 789L;

        when(postRepository.createPost(any(CreatePostRequest.class))).thenReturn(expectedPostId);

        CreatePostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Post created successfully", response.getMessage());
        assertEquals(expectedPostId, response.getPostId());

        verify(postRepository).createPost(request);
    }

    @Test
    public void testCreatePost_InvalidUserId() throws Exception {
        CreatePostRequest request = createSamplePostRequest();
        request.setUserId(0); // Invalid user ID

        CreatePostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Missing or invalid userID", response.getMessage());
        assertNull(response.getPostId());

        verify(postRepository, never()).createPost(any(CreatePostRequest.class));
    }

    @Test
    public void testCreatePost_DatabaseError() throws Exception {
        CreatePostRequest request = createSamplePostRequest();

        when(postRepository.createPost(any(CreatePostRequest.class)))
                .thenThrow(new SQLException("Database connection error"));

        Exception exception = assertThrows(Exception.class, () -> {
            postService.createPost(request);
        });

        assertEquals("Failed to create post: Database connection error", exception.getMessage());

        verify(postRepository).createPost(request);
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

    private CreatePostRequest createSamplePollRequest() {
        CreatePostRequest request = createSamplePostRequest();
        request.setPoll(true);
        request.setPollQuestion("What is your favorite color?");
        request.setPollDurationType("days");
        request.setPollDurationLength(3);
        request.setPollOptions(Arrays.asList("Red", "Blue", "Green", "Yellow"));
        return request;
    }

    private CreatePostRequest createSampleShareRequest() {
        CreatePostRequest request = createSamplePostRequest();
        request.setOriginalPostId(42L);
        request.setContentText("Sharing this post");
        return request;
    }
}
