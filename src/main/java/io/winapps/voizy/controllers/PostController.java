package io.winapps.voizy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.CreatePostResponse;
import io.winapps.voizy.models.posts.GetPostMediaResponse;
import io.winapps.voizy.models.posts.ListPostsResponse;
import io.winapps.voizy.services.PostService;
import io.winapps.voizy.util.JsonUtil;
import io.winapps.voizy.util.ServletAdapter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;

public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final PostService postService;

    public PostController() {
        this.postService = new PostService();
    }

    public PostController(PostService postService) {
        this.postService = postService;
    }

    public void listPosts(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("GET")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            String userIdString = req.getParameter("id");
            if (userIdString == null || userIdString.isEmpty()) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'id'");
                return;
            }

            String limitString = req.getParameter("limit");
            if (limitString == null || limitString.isEmpty()) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'limit'");
                return;
            }

            String pageString = req.getParameter("page");
            if (pageString == null || pageString.isEmpty()) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'page'");
                return;
            }

            long userId, limit, page;
            try {
                userId = Long.parseLong(userIdString);
                limit = Long.parseLong(limitString);
                page = Long.parseLong(pageString);
            } catch (NumberFormatException e) {
                logger.error("Error parsing request parameters", e);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters: " + e.getMessage());
                return;
            }

            ListPostsResponse response = postService.listPosts(userId, limit, page);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), response);

        } catch (Exception e) {
            logger.error("Error listing posts", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error listing posts: " + e.getMessage());
        }
    }

    public void createPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("POST")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            CreatePostRequest request = objectMapper.readValue(req.getInputStream(), CreatePostRequest.class);

            if (request.getUserId() <= 0) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid userID");
                return;
            }

            CreatePostResponse response = postService.createPost(request);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), response);

        } catch (Exception e) {
            logger.error("Error creating post", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating post: " + e.getMessage());
        }
    }

    public BiConsumer<HttpServletRequest, HttpServletResponse> listPostsHandler() {
        return (req, res) -> {
            try {
                listPosts(req, res);
            } catch (IOException e) {
                logger.error("IO error in list posts handler", e);
                throw new RuntimeException("Error handling list posts request", e);
            }
        };
    }

    public BiConsumer<HttpServletRequest, HttpServletResponse> createPostHandler() {
        return (req, res) -> {
            try {
                createPost(req, res);
            } catch (IOException e) {
                logger.error("IO error in create post handler", e);
                throw new RuntimeException("Error handling create post request", e);
            }
        };
    }

    public HttpServlet listPostsServlet() {
        return new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    listPosts(req, resp);
                } catch (IOException e) {
                    logger.error("IO error in list posts servlet", e);
                    throw new RuntimeException("Error handling list posts request", e);
                }
            }
        };
    }

    public HttpServlet createPostServlet() {
        return new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    createPost(req, resp);
                } catch (IOException e) {
                    logger.error("IO error in create post servlet", e);
                    throw new RuntimeException("Error handling create post request", e);
                }
            }
        };
    }

    public void getPostMedia(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("GET")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            String postIdString = req.getParameter("id");
            if (postIdString == null || postIdString.isEmpty()) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter 'id'");
                return;
            }

            long postId;
            try {
                postId = Long.parseLong(postIdString);
            } catch (NumberFormatException e) {
                logger.error("Error parsing post ID", e);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID: " + e.getMessage());
                return;
            }

            GetPostMediaResponse response = postService.getPostMedia(postId);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), response);

        } catch (Exception e) {
            logger.error("Error getting post media", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error getting post media: " + e.getMessage());
        }
    }

    public BiConsumer<HttpServletRequest, HttpServletResponse> getPostMediaHandler() {
        return (req, res) -> {
            try {
                getPostMedia(req, res);
            } catch (IOException e) {
                logger.error("IO error in get post media handler", e);
                throw new RuntimeException("Error handling get post media request", e);
            }
        };
    }

    public HttpServlet getPostMediaServlet() {
        return new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    getPostMedia(req, resp);
                } catch (IOException e) {
                    logger.error("IO error in get post media servlet", e);
                    throw new RuntimeException("Error handling get post media request", e);
                }
            }
        };
    }
}
