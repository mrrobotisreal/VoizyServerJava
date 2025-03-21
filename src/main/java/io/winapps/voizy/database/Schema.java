package io.winapps.voizy.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Schema {
    private static final Logger logger = LoggerFactory.getLogger(Schema.class);

    public static void createTables() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            // API Keys table
            stmt.execute("CREATE TABLE IF NOT EXISTS api_keys (" +
                    "api_key_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "api_key VARCHAR(255) NOT NULL UNIQUE, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "expires_at DATETIME NOT NULL, " +
                    "last_used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "api_key VARCHAR(255) NOT NULL UNIQUE, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "salt VARCHAR(255) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            // User Profiles table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_profiles (" +
                    "profile_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "first_name VARCHAR(100), " +
                    "last_name VARCHAR(100), " +
                    "preferred_name VARCHAR(100), " +
                    "birth_date DATE, " +
                    "city_of_residence VARCHAR(255), " +
                    "place_of_work VARCHAR(255), " +
                    "date_joined DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // User Schools table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_schools (" +
                    "user_school_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "school_name VARCHAR(255) NOT NULL, " +
                    "start_year INT, " +
                    "end_year INT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Interests table
            stmt.execute("CREATE TABLE IF NOT EXISTS interests (" +
                    "interest_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL UNIQUE" +
                    ")");

            // User Interests table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_interests (" +
                    "user_interest_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "interest_id BIGINT NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (interest_id) REFERENCES interests(interest_id) ON DELETE CASCADE" +
                    ")");

            // User Social Links table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_social_links (" +
                    "link_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "platform VARCHAR(100) NOT NULL, " +
                    "url VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // User Images table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_images (" +
                    "user_image_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "image_url VARCHAR(255) NOT NULL, " +
                    "is_profile_pic BOOLEAN NOT NULL DEFAULT 0, " +
                    "is_cover_pic BOOLEAN NOT NULL DEFAULT 0, " +
                    "uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Songs table
            stmt.execute("CREATE TABLE IF NOT EXISTS songs (" +
                    "song_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "artist VARCHAR(255) NOT NULL, " +
                    "song_url VARCHAR(255) NOT NULL" +
                    ")");

            // User Songs table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_songs (" +
                    "user_song_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "song_id BIGINT NOT NULL, " +
                    "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE" +
                    ")");

            // Friendships table
            stmt.execute("CREATE TABLE IF NOT EXISTS friendships (" +
                    "friendship_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "friend_id BIGINT NOT NULL, " +
                    "status ENUM('pending','accepted','blocked') NOT NULL DEFAULT 'pending', " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Groups table
            stmt.execute("CREATE TABLE IF NOT EXISTS groups_table (" +
                    "group_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "privacy ENUM('public','private','closed') NOT NULL DEFAULT 'public', " +
                    "creator_id BIGINT NOT NULL, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Group Members table
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_member_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "group_id BIGINT NOT NULL, " +
                    "user_id BIGINT NOT NULL, " +
                    "role ENUM('member','moderator','admin') NOT NULL DEFAULT 'member', " +
                    "joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (group_id) REFERENCES groups_table(group_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Posts table
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                    "post_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "to_user_id BIGINT NOT NULL DEFAULT -1, " +
                    "original_post_id BIGINT NULL DEFAULT NULL, " +
                    "impressions BIGINT NOT NULL DEFAULT 0, " +
                    "views BIGINT NOT NULL DEFAULT 0, " +
                    "content_text TEXT, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "location_name VARCHAR(255), " +
                    "location_lat DECIMAL(9,6), " +
                    "location_lng DECIMAL(9,6), " +
                    "is_poll BOOLEAN NOT NULL DEFAULT 0, " +
                    "poll_question VARCHAR(255), " +
                    "poll_duration_type ENUM('hours','days','weeks') DEFAULT 'days', " +
                    "poll_duration_length INT DEFAULT 1, " +
                    "poll_end_datetime DATETIME, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (original_post_id) REFERENCES posts(post_id) ON DELETE SET NULL" +
                    ")");

            createAdditionalTables(stmt);

            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Error creating tables", e);
            throw e;
        }
    }

    private static void createAdditionalTables(Statement stmt) throws SQLException {
        // Poll Options table
        stmt.execute("CREATE TABLE IF NOT EXISTS poll_options (" +
                "poll_option_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "option_text VARCHAR(255) NOT NULL, " +
                "vote_count INT DEFAULT 0, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE" +
                ")");

        // Poll Votes table
        stmt.execute("CREATE TABLE IF NOT EXISTS poll_votes (" +
                "poll_vote_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "poll_option_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "voted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (poll_option_id) REFERENCES poll_options(poll_option_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Hashtags table
        stmt.execute("CREATE TABLE IF NOT EXISTS hashtags (" +
                "hashtag_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tag VARCHAR(255) NOT NULL UNIQUE, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Post Hashtags table
        stmt.execute("CREATE TABLE IF NOT EXISTS post_hashtags (" +
                "post_hashtag_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "hashtag_id BIGINT NOT NULL, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (hashtag_id) REFERENCES hashtags(hashtag_id) ON DELETE CASCADE" +
                ")");

        // Post Reactions table
        stmt.execute("CREATE TABLE IF NOT EXISTS post_reactions (" +
                "post_reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "reaction_type ENUM('like','love','laugh','congratulate','shocked','sad','angry') NOT NULL, " +
                "reacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Comments table
        stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                "comment_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "content_text TEXT NOT NULL, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Comment Reactions table
        stmt.execute("CREATE TABLE IF NOT EXISTS comment_reactions (" +
                "comment_reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "comment_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "reaction_type ENUM('like','love','laugh','congratulate','shocked','sad','angry') NOT NULL, " +
                "reacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Post Shares table
        stmt.execute("CREATE TABLE IF NOT EXISTS post_shares (" +
                "share_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "shared_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Post Media table
        stmt.execute("CREATE TABLE IF NOT EXISTS post_media (" +
                "media_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "post_id BIGINT NOT NULL, " +
                "media_url VARCHAR(255) NOT NULL, " +
                "media_type ENUM('image','video') NOT NULL, " +
                "uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE" +
                ")");

        // Conversations table
        stmt.execute("CREATE TABLE IF NOT EXISTS conversations (" +
                "conversation_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "conversation_name VARCHAR(255), " +
                "is_group_chat BOOLEAN NOT NULL DEFAULT 0, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Conversation Members table
        stmt.execute("CREATE TABLE IF NOT EXISTS conversation_members (" +
                "conv_member_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "conversation_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Messages table
        stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                "message_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "conversation_id BIGINT NOT NULL, " +
                "sender_id BIGINT NOT NULL, " +
                "content_text TEXT, " +
                "sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Message Recipients table
        stmt.execute("CREATE TABLE IF NOT EXISTS message_recipients (" +
                "msg_recipient_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "message_id BIGINT NOT NULL, " +
                "recipient_id BIGINT NOT NULL, " +
                "is_read BOOLEAN NOT NULL DEFAULT 0, " +
                "read_at DATETIME, " +
                "FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Message Attachments table
        stmt.execute("CREATE TABLE IF NOT EXISTS message_attachments (" +
                "attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "message_id BIGINT NOT NULL, " +
                "file_url VARCHAR(255) NOT NULL, " +
                "file_type ENUM('image','video','doc') DEFAULT 'image', " +
                "uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE" +
                ")");

        // Message Reactions table
        stmt.execute("CREATE TABLE IF NOT EXISTS message_reactions (" +
                "message_reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "message_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "reaction_type ENUM('like','love','laugh','congratulate','shocked','sad','angry') NOT NULL, " +
                "reacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");

        // Analytics Events table
        stmt.execute("CREATE TABLE IF NOT EXISTS analytics_events (" +
                "event_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "event_type VARCHAR(100) NOT NULL, " +
                "object_type VARCHAR(100), " +
                "object_id BIGINT, " +
                "event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "metadata JSON, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")");
    }
}
