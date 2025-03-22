package io.winapps.voizy.repositories;

import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.models.posts.CreatePostRequest;
import io.winapps.voizy.models.posts.ListPost;
import io.winapps.voizy.util.SqlNullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);

    public long getTotalPostsCount(long userId) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM posts WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countQuery)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }

    public List<ListPost> listPosts(long userId, long limit, long offset) throws SQLException {
        String selectQuery = "SELECT " +
                "p.post_id, " +
                "p.user_id, " +
                "p.to_user_id, " +
                "p.original_post_id, " +
                "p.impressions, " +
                "p.views, " +
                "p.content_text, " +
                "p.created_at, " +
                "p.updated_at, " +
                "p.location_name, " +
                "p.location_lat, " +
                "p.location_lng, " +
                "p.is_poll, " +
                "p.poll_question, " +
                "p.poll_duration_type, " +
                "p.poll_duration_length, " +
                "u.username, " +
                "up.first_name, " +
                "up.last_name, " +
                "up.preferred_name, " +
                "pr_user.reaction_type AS user_reaction, " +
                "(SELECT COUNT(*) FROM post_reactions pr WHERE pr.post_id = p.post_id) AS total_reactions, " +
                "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS total_comments, " +
                "(SELECT COUNT(*) FROM post_shares ps WHERE ps.post_id = p.post_id) AS total_post_shares " +
                "FROM posts p LEFT JOIN users u ON u.user_id = p.user_id " +
                "LEFT JOIN user_profiles up ON up.user_id = p.user_id " +
                "LEFT JOIN post_reactions pr_user ON pr_user.post_id = p.post_id AND pr_user.user_id = ? " +
                "WHERE (p.user_id = ? OR p.to_user_id = ?) " +
                "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";

        List<ListPost> posts = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setLong(3, userId);
            stmt.setLong(4, limit);
            stmt.setLong(5, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ListPost post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
        }

        return posts;
    }

    private ListPost mapResultSetToPost(ResultSet rs) throws SQLException {
        ListPost post = new ListPost();

        post.setPostId(rs.getLong("post_id"));
        post.setUserId(rs.getLong("user_id"));
        post.setToUserId(rs.getLong("to_user_id"));
        post.setOriginalPostId(SqlNullUtil.getLong(rs, "original_post_id"));
        post.setImpressions(rs.getLong("impressions"));
        post.setViews(rs.getLong("views"));
        post.setContentText(SqlNullUtil.getString(rs, "content_text"));
        post.setCreatedAt(SqlNullUtil.getLocalDateTime(rs, "created_at"));
        post.setUpdatedAt(SqlNullUtil.getLocalDateTime(rs, "updated_at"));
        post.setLocationName(SqlNullUtil.getString(rs, "location_name"));
        post.setLocationLat(SqlNullUtil.getDouble(rs, "location_lat"));
        post.setLocationLong(SqlNullUtil.getDouble(rs, "location_lng"));
        post.setIsPoll(SqlNullUtil.getBoolean(rs, "is_poll"));
        post.setPollQuestion(SqlNullUtil.getString(rs, "poll_question"));
        post.setPollDurationType(SqlNullUtil.getString(rs, "poll_duration_type"));
        post.setPollDurationLength(SqlNullUtil.getLong(rs, "poll_duration_length"));
        post.setUsername(SqlNullUtil.getString(rs, "username"));
        post.setFirstName(SqlNullUtil.getString(rs, "first_name"));
        post.setLastName(SqlNullUtil.getString(rs, "last_name"));
        post.setPreferredName(SqlNullUtil.getString(rs, "preferred_name"));
        post.setUserReaction(SqlNullUtil.getString(rs, "user_reaction"));
        post.setTotalReactions(rs.getLong("total_reactions"));
        post.setTotalComments(rs.getLong("total_comments"));
        post.setTotalPostShares(rs.getLong("total_post_shares"));

        return post;
    }

    public long createPost(CreatePostRequest request) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            long postId = insertPost(conn, request);

            if (request.getOriginalPostId() != null) {
                insertSharedPost(conn, request.getOriginalPostId(), request.getUserId());
            }

            if (request.isPoll() && request.getPollOptions() != null && !request.getPollOptions().isEmpty()) {
                insertPollOptions(conn, postId, request.getPollOptions());
            }

            if (request.getImages() != null && !request.getImages().isEmpty()) {
                insertPostMedia(conn, postId, request.getImages());
            }

            if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
                insertPostHashtags(conn, postId, request.getHashtags());
            }

            conn.commit();
            return postId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    private long insertPost(Connection conn, CreatePostRequest request) throws SQLException {
        String query;
        PreparedStatement stmt = null;

        try {
            if (!request.isPoll()) {
                if (request.getOriginalPostId() != null) {
                    query = "INSERT INTO posts (" +
                    "user_id, to_user_id, original_post_id, content_text, " +
                    "location_name, location_lat, location_lng, is_poll) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong(1, request.getUserId());
                    stmt.setLong(2, request.getToUserId());
                    stmt.setLong(3, request.getOriginalPostId());
                    stmt.setString(4, request.getContentText());
                    stmt.setString(5, request.getLocationName());
                    stmt.setDouble(6, request.getLocationLat());
                    stmt.setDouble(7, request.getLocationLong());
                    stmt.setBoolean(8, false);
                } else {
                    query = "INSERT INTO posts (" +
                    "user_id, to_user_id, content_text, location_name, location_lat, location_lng, is_poll) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong(1, request.getUserId());
                    stmt.setLong(2, request.getToUserId());
                    stmt.setString(3, request.getContentText());
                    stmt.setString(4, request.getLocationName());
                    stmt.setDouble(5, request.getLocationLat());
                    stmt.setDouble(6, request.getLocationLong());
                    stmt.setBoolean(7, false);
                }
            } else {
                if (request.getOriginalPostId() != null) {
                    query = "INSERT INTO posts (" +
                    "user_id, to_user_id, original_post_id, content_text, location_name, location_lat, " +
                    "location_lng, is_poll, poll_question, poll_duration_type, poll_duration_length) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong(1, request.getUserId());
                    stmt.setLong(2, request.getToUserId());
                    stmt.setLong(3, request.getOriginalPostId());
                    stmt.setString(4, request.getContentText());
                    stmt.setString(5, request.getLocationName());
                    stmt.setDouble(6, request.getLocationLat());
                    stmt.setDouble(7, request.getLocationLong());
                    stmt.setBoolean(8, true);
                    stmt.setString(9, request.getPollQuestion());
                    stmt.setString(10, request.getPollDurationType());
                    stmt.setLong(11, request.getPollDurationLength());
                } else {
                    query = "INSERT INTO posts (" +
                    "user_id, to_user_id, content_text, location_name, location_lat, location_lng, " +
                    "is_poll, poll_question, poll_duration_type, poll_duration_length) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong(1, request.getUserId());
                    stmt.setLong(2, request.getToUserId());
                    stmt.setString(3, request.getContentText());
                    stmt.setString(4, request.getLocationName());
                    stmt.setDouble(5, request.getLocationLat());
                    stmt.setDouble(6, request.getLocationLong());
                    stmt.setBoolean(7, true);
                    stmt.setString(8, request.getPollQuestion());
                    stmt.setString(9, request.getPollDurationType());
                    stmt.setLong(10, request.getPollDurationLength());
                }
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating post failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating post failed, no ID obtained.");
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private void insertSharedPost(Connection conn, long originalPostId, long userId) throws SQLException {
        String query = "INSERT INTO post_shares (post_id, user_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, originalPostId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    private void insertPollOptions(Connection conn, long postId, List<String> options) throws SQLException {
        if (options == null || options.isEmpty()) {
            logger.info("No options were passed to insertPollOptions");
            return;
        }

        String query = "INSERT INTO poll_options (post_id, option_text) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String option : options) {
                stmt.setLong(1, postId);
                stmt.setString(2, option);
                stmt.executeUpdate();
            }
        }
    }

    private void insertPostMedia(Connection conn, long postId, List<String> images) throws SQLException {
        if (images == null || images.isEmpty()) {
            logger.info("No images were passed to insertPostMedia");
            return;
        }

        String query = "INSERT INTO post_media (post_id, media_url, media_type) VALUES (?, ?, 'image')";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String imageUrl : images) {
                stmt.setLong(1, postId);
                stmt.setString(2, imageUrl);
                stmt.executeUpdate();
            }
        }
    }

    private void insertPostHashtags(Connection conn, long postId, List<String> hashtags) throws SQLException {
        if (hashtags == null || hashtags.isEmpty()) {
            logger.info("No hashtags were passed to insertPostHashtags");
            return;
        }

        String upsertTagQuery = "INSERT INTO hashtags (tag) VALUES (?) ON DUPLICATE KEY UPDATE tag=VALUES(tag)";
        String selectTagQuery = "SELECT hashtag_id FROM hashtags WHERE tag = ? LIMIT 1";
        String insertPostHashtagQuery = "INSERT INTO post_hashtags (post_id, hashtag_id) VALUES (?, ?)";

        try (
                PreparedStatement upsertTagStmt = conn.prepareStatement(upsertTagQuery);
                PreparedStatement selectTagStmt = conn.prepareStatement(selectTagQuery);
                PreparedStatement insertPostHashtagStmt = conn.prepareStatement(insertPostHashtagQuery)
        ) {
            for (String tag : hashtags) {
                String cleanedTag = tag;
                if (cleanedTag != null && !cleanedTag.isEmpty() && cleanedTag.charAt(0) == '#') {
                    cleanedTag = cleanedTag.substring(1);
                }

                upsertTagStmt.setString(1, cleanedTag);
                upsertTagStmt.executeUpdate();

                selectTagStmt.setString(1, cleanedTag);
                try (ResultSet rs = selectTagStmt.executeQuery()) {
                    if (rs.next()) {
                        long tagId = rs.getLong("hashtag_id");

                        insertPostHashtagStmt.setLong(1, postId);
                        insertPostHashtagStmt.setLong(2, tagId);
                        insertPostHashtagStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
