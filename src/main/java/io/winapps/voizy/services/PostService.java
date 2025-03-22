package io.winapps.voizy.services;

import io.winapps.voizy.models.posts.ListPost;
import io.winapps.voizy.models.posts.ListPostsResponse;
import io.winapps.voizy.repositories.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

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
}
