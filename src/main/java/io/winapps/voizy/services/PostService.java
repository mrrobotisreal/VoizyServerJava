package io.winapps.voizy.services;

import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.CreatePostResponse;
import io.winapps.voizy.models.posts.ListPost;
import io.winapps.voizy.models.posts.ListPostsResponse;
import io.winapps.voizy.repositories.PostRepository;
import io.winapps.voizy.util.AnalyticsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    public PostService() {
        this.postRepository = new PostRepository();
    }

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public ListPostsResponse listPosts(long userId, long limit, long page) throws Exception {
        try {
            long offset = (page - 1) * limit;

            long totalPosts = postRepository.getTotalPostsCount(userId);

            List<ListPost> posts = postRepository.listPosts(userId, limit, offset);

            long totalPages = (long) Math.ceil((double) totalPosts / limit);

            ListPostsResponse response = new ListPostsResponse();
            response.setPosts(posts);
            response.setLimit(limit);
            response.setPage(page);
            response.setTotalPosts(totalPosts);
            response.setTotalPages(totalPages);

            return response;
        } catch (SQLException e) {
            logger.error("Database error while listing posts", e);
            throw new Exception("Failed to list posts: " + e.getMessage());
        }
    }

    public CreatePostResponse createPost(CreatePostRequest request) throws Exception {
        try {
            if (request.getUserId() <= 0) {
                throw new IllegalArgumentException("Missing or invalid userID");
            }

            long postId = postRepository.createPost(request);

            trackPostCreationEvents(request, postId);

            CreatePostResponse response = new CreatePostResponse();
            response.setSuccess(true);
            response.setMessage("Post created successfully");
            response.setPostId(postId);

            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error while creating post", e);
            CreatePostResponse response = new CreatePostResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        } catch (SQLException e) {
            logger.error("Database error while creating post", e);
            throw new Exception("Failed to create post: " + e.getMessage());
        }
    }

    private void trackPostCreationEvents(CreatePostRequest request, long postId) {
        AnalyticsUtil.trackEvent(request.getUserId(), "create_post", "post", postId, null);

        if (request.getOriginalPostId() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("shared_post_id", postId);
            AnalyticsUtil.trackEvent(request.getUserId(), "share_post", "post", request.getOriginalPostId(), metadata);
        }
    }
}
