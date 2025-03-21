package io.winapps.voizy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;
    private static JedisPool jedisPool;

    public static void initMySQL() throws SQLException {
        try {
            String username = System.getenv("DBU");
            String password = System.getenv("DBP");
            String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/voizy?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(10000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection()) {
                if (!conn.isValid(5)) {
                    throw new SQLException("Failed to validate database connection");
                }
            }

            Schema.createTables();

            logger.info("MySQL connected and schema ensured");
        } catch (SQLException e) {
            logger.error("Failed to initialize MySQL", e);
            throw e;
        }
    }

    public static void initRedis() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);

            jedisPool = new JedisPool(poolConfig, "localhost", 6379, 2000);

            try (var jedis = jedisPool.getResource()) {
                jedis.ping();
            }

            logger.info("Redis connected");
        } catch (Exception e) {
            logger.error("Failed to initialize Redis");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }

    public static JedisPool getJedisPool() {
        if (jedisPool == null) {
            throw new IllegalStateException("Redis not initialized");
        }
        return jedisPool;
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }

        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
