package io.winapps.voizy.util;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Utility class for handling SQL NULL values in JDBC result sets
 */
public class SqlNullUtil {
    /**
     * Get a String from a ResultSet, returning null if the value is SQL NULL
     */
    public static String getString(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get a Long from a ResultSet, returning null if the value is SQL NULL
     */
    public static Long getLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get an Integer from a ResultSet, returning null if the value is SQL NULL
     */
    public static Integer getInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get a Double from a ResultSet, returning null if the value is SQL NULL
     */
    public static Double getDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get a Boolean from a ResultSet, returning null if the value is SQL NULL
     */
    public static Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get a LocalDate from a ResultSet, returning null if the value is SQL NULL
     */
    public static LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return date == null ? null : date.toLocalDate();
    }

    /**
     * Get a LocalDateTime from a ResultSet, returning null if the value is SQL NULL
     */
    public static LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    /**
     * Convert a nullable value to an Optional
     */
    public static <T> Optional<T> toOptional(T value) {
        return Optional.ofNullable(value);
    }
}
