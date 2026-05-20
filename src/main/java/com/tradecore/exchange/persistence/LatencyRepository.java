package com.tradecore.exchange.persistence;

import com.tradecore.exchange.metrics.LatencyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class LatencyRepository {
    private static final Logger logger = LoggerFactory.getLogger(LatencyRepository.class);

    public static void insert(LatencyRecord record, String sessionId) {
        String sql = """
            INSERT INTO latency_metrics (metric_id, order_id, session_id, validation_latency_us, sequencing_latency_us, matching_latency_us, publishing_latency_us, total_latency_us)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, record.getOrderId().toString());
            stmt.setString(3, sessionId);
            stmt.setLong(4, record.getValidationLatencyMicros());
            stmt.setLong(5, record.getSequencingLatencyMicros());
            stmt.setLong(6, record.getMatchingLatencyMicros());
            stmt.setLong(7, record.getPublishingLatencyMicros());
            stmt.setLong(8, record.getTotalLatencyMicros());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("[DB] Failed to insert latency record", e);
        }
    }
}