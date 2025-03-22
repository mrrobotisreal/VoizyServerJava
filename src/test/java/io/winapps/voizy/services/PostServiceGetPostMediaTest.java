package io.winapps.voizy.services;

import io.winapps.voizy.models.posts.GetPostMediaResponse;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostServiceGetPostMediaTest {
    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    public void setup() {
        postService = new PostService(postRepository);
    }

    @Test
    public void testGetPostMedia_Success() throws Exception {
        long postId = 123L;
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");
        List<String> videos = Collections.emptyList();

        GetPostMediaResponse mockResponse = new GetPostMediaResponse();
        mockResponse.setImages(images);
        mockResponse.setVideos(videos);

        when(postRepository.getPostMedia(postId)).thenReturn(mockResponse);

        GetPostMediaResponse response = postService.getPostMedia(postId);

        assertNotNull(response);
        assertEquals(images, response.getImages());
        assertEquals(videos, response.getVideos());

        verify(postRepository).getPostMedia(postId);
    }

    @Test
    public void testGetPostMedia_WithVideos() throws Exception {
        long postId = 456L;
        List<String> images = Arrays.asList("image1.jpg");
        List<String> videos = Arrays.asList("video1.mp4");

        GetPostMediaResponse mockResponse = new GetPostMediaResponse();
        mockResponse.setImages(images);
        mockResponse.setVideos(videos);

        when(postRepository.getPostMedia(postId)).thenReturn(mockResponse);

        GetPostMediaResponse response = postService.getPostMedia(postId);

        assertNotNull(response);
        assertEquals(images, response.getImages());
        assertEquals(videos, response.getVideos());

        verify(postRepository).getPostMedia(postId);
    }

    @Test
    public void testGetPostMedia_EmptyResponse() throws Exception {
        long postId = 789L;
        List<String> images = Collections.emptyList();
        List<String> videos = Collections.emptyList();

        GetPostMediaResponse mockResponse = new GetPostMediaResponse();
        mockResponse.setImages(images);
        mockResponse.setVideos(videos);

        when(postRepository.getPostMedia(postId)).thenReturn(mockResponse);

        GetPostMediaResponse response = postService.getPostMedia(postId);

        assertNotNull(response);
        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getVideos().isEmpty());

        verify(postRepository).getPostMedia(postId);
    }

    @Test
    public void testGetPostMedia_DatabaseError() throws Exception {
        long postId = 999L;

        when(postRepository.getPostMedia(postId)).thenThrow(new SQLException("Database error"));

        GetPostMediaResponse response = postService.getPostMedia(postId);

        assertNotNull(response);
        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getVideos().isEmpty());

        verify(postRepository).getPostMedia(postId);
    }
}
