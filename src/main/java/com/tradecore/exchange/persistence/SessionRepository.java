package com.tradecore.exchange.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SessionRepository {
    private static final Logger logger = LoggerFactory.getLogger(SessionRepository.class);

    public static void insert(String sessionId, String engineVersion) {
        String sql = "INSERT INTO sessions (session_id, engine_version) VALUES (?, ?)";
        try (
                Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, sessionId);
            stmt.setString(2, engineVersion);
            stmt.executeUpdate();

            logger.info("[DB] Session created: {}", sessionId);
        } catch (SQLException e) {
            logger.error("[DB] Failed to insert session", e);
        }
    }

    public static void update(String sessionId, double runtimeSeconds, long totalSubmitted, long totalProcessed, double throughput) {
        String sql = """
            UPDATE sessions SET
                runtime_seconds = ?,
                total_orders_submitted = ?,
                total_orders_processed = ?,
                throughput = ?
            WHERE session_id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, runtimeSeconds);
            stmt.setLong(2, totalSubmitted);
            stmt.setLong(3, totalProcessed);
            stmt.setDouble(4, throughput);
            stmt.setString(5, sessionId);
            stmt.executeUpdate();

            logger.info("[DB] Session updated: {}", sessionId);
        } catch (SQLException e) {
            logger.error("[DB] Failed to update session", e);
        }
    }
}