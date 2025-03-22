package io.winapps.voizy.controllers;

import io.winapps.voizy.models.posts.ListPost;
import io.winapps.voizy.models.posts.ListPostsResponse;
import io.winapps.voizy.services.PostService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {
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
    public void setup() {
        postController = new PostController(postService);
    }

    @Test
    public void testListPosts_Success() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("limit")).thenReturn("10");
        when(request.getParameter("page")).thenReturn("1");

        when(response.getOutputStream()).thenReturn(outputStream);

        ListPostsResponse mockResponse = new ListPostsResponse();
        mockResponse.setPosts(createMockPosts(5));
        mockResponse.setLimit(10);
        mockResponse.setPage(1);
        mockResponse.setTotalPosts(20);
        mockResponse.setTotalPages(2);
        when(postService.listPosts(1L, 10L, 1L)).thenReturn(mockResponse);

        postController.listPosts(request, response);

        verify(response).setContentType("application/json");
        verify(postService).listPosts(1L, 10L, 1L);
        verify(response, never()).sendError(anyInt(), anyString());
        verify(response).getOutputStream();
    }

    @Test
    public void testListPosts_MissingIdParameter() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn(null);

        postController.listPosts(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'id'");
        verify(postService, never()).listPosts(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testListPosts_MissingLimitParameter() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("limit")).thenReturn(null);

        postController.listPosts(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'limit'");
        verify(postService, never()).listPosts(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testListPosts_MissingPageParameter() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("limit")).thenReturn("10");
        when(request.getParameter("page")).thenReturn(null);

        postController.listPosts(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'page'");
        verify(postService, never()).listPosts(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testListPosts_InvalidMethod() throws Exception {
        when(request.getMethod()).thenReturn("POST"); // Wrong method

        postController.listPosts(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
        verify(postService, never()).listPosts(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testListPosts_InvalidUserId() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("not-a-number");
        when(request.getParameter("limit")).thenReturn("10");
        when(request.getParameter("page")).thenReturn("1");

        postController.listPosts(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid parameters"));
        verify(postService, never()).listPosts(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testListPosts_ServiceException() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("limit")).thenReturn("10");
        when(request.getParameter("page")).thenReturn("1");

        when(postService.listPosts(eq(1L), eq(10L), eq(1L))).thenThrow(new RuntimeException("Service error"));

        postController.listPosts(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error listing posts"));
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
