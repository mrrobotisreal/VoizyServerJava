package io.winapps.voizy.repositories;

import io.winapps.voizy.database.DatabaseManager;
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
}
