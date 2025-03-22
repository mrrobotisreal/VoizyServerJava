package io.winapps.voizy.services;

import io.winapps.voizy.models.posts.ListPost;
import io.winapps.voizy.models.posts.ListPostsResponse;
import io.winapps.voizy.repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    public void setup() {
        postService = new PostService(postRepository);
    }

    @Test
    public void testListPosts_Success() throws Exception {
        long userId = 1L;
        long limit = 10L;
        long page = 1L;
        long offset = 0L; // (page - 1) * limit
        long totalPosts = 25L;

        List<ListPost> mockPosts = createMockPosts(5);

        when(postRepository.getTotalPostsCount(userId)).thenReturn(totalPosts);
        when(postRepository.listPosts(userId, limit, offset)).thenReturn(mockPosts);

        ListPostsResponse response = postService.listPosts(userId, limit, page);

        assertNotNull(response);
        assertEquals(mockPosts, response.getPosts());
        assertEquals(limit, response.getLimit());
        assertEquals(page, response.getPage());
        assertEquals(totalPosts, response.getTotalPosts());
        assertEquals(3L, response.getTotalPages()); // Ceil(25/10) = 3

        verify(postRepository).getTotalPostsCount(userId);
        verify(postRepository).listPosts(userId, limit, offset);
    }

    @Test
    public void testListPosts_EmptyResult() throws Exception {
        long userId = 1L;
        long limit = 10L;
        long page = 1L;
        long offset = 0L;
        long totalPosts = 0L;

        when(postRepository.getTotalPostsCount(userId)).thenReturn(totalPosts);
        when(postRepository.listPosts(userId, limit, offset)).thenReturn(new ArrayList<>());

        ListPostsResponse response = postService.listPosts(userId, limit, page);

        assertNotNull(response);
        assertTrue(response.getPosts().isEmpty());
        assertEquals(limit, response.getLimit());
        assertEquals(page, response.getPage());
        assertEquals(totalPosts, response.getTotalPosts());
        assertEquals(0L, response.getTotalPages());
    }

    @Test
    public void testListPosts_DatabaseError() {
        long userId = 1L;
        long limit = 10L;
        long page = 1L;

        try {
            when(postRepository.getTotalPostsCount(userId)).thenThrow(new SQLException("Database error"));

            Exception exception = assertThrows(Exception.class, () -> {
                postService.listPosts(userId, limit, page);
            });

            assertEquals("Failed to list posts: Database error", exception.getMessage());

            verify(postRepository).getTotalPostsCount(userId);
            verify(postRepository, never()).listPosts(anyLong(), anyLong(), anyLong());
        } catch (SQLException e) {
            fail("Test setup failed", e);
        }
    }

    @Test
    public void testListPosts_Pagination() throws Exception {
        long userId = 1L;
        long limit = 5L;
        long page = 3L;
        long offset = 10L; // (page - 1) * limit
        long totalPosts = 22L;

        List<ListPost> mockPosts = createMockPosts(2); // Only 2 posts on the last page

        when(postRepository.getTotalPostsCount(userId)).thenReturn(totalPosts);
        when(postRepository.listPosts(userId, limit, offset)).thenReturn(mockPosts);

        ListPostsResponse response = postService.listPosts(userId, limit, page);

        assertNotNull(response);
        assertEquals(mockPosts, response.getPosts());
        assertEquals(limit, response.getLimit());
        assertEquals(page, response.getPage());
        assertEquals(totalPosts, response.getTotalPosts());
        assertEquals(5L, response.getTotalPages()); // Ceil(22/5) = 5

        verify(postRepository).getTotalPostsCount(userId);
        verify(postRepository).listPosts(userId, limit, offset);
    }

    private List<ListPost> createMockPosts(int count) {
        List<ListPost> posts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ListPost post = new ListPost();
            post.setPostId(i + 1);
            post.setUserId(1L);
            post.setToUserId(-1L);
            post.setImpressions(100L + i);
            post.setViews(50L + i);
            post.setContentText("Test post content " + (i + 1));
            post.setCreatedAt(LocalDateTime.now().minusDays(i));
            post.setUsername("testuser");
            post.setPreferredName("Test User");
            post.setTotalReactions(10L + i);
            post.setTotalComments(5L + i);

            posts.add(post);
        }

        return posts;
    }
}
