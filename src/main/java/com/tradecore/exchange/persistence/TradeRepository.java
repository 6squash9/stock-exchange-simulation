package com.tradecore.exchange.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class TradeRepository {
    private static final Logger logger = LoggerFactory.getLogger(TradeRepository.class);

    public static void insert(String buyOrderId, String sellOrderId,
                              String instrument, String matchType,
                              double price, double quantity, String sessionId) {
        String sql = """
            INSERT INTO trades (trade_id, buy_order_id, sell_order_id, instrument, match_type, price, quantity, session_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, buyOrderId);
            stmt.setString(3, sellOrderId);
            stmt.setString(4, instrument);
            stmt.setString(5, matchType);
            stmt.setDouble(6, price);
            stmt.setDouble(7, quantity);
            stmt.setString(8, sessionId);
            stmt.executeUpdate();

            logger.info("[DB] Trade saved: {} @ {}", quantity, price);
        } catch (SQLException e) {
            logger.error("[DB] Failed to insert trade", e);
        }
    }
}