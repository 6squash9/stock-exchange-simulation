package com.tradecore.exchange.persistence;

import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.order.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    public static void insert(ISimpleOrder order, String botId, String sessionId) {
        String sql = """
            INSERT INTO orders (order_id, bot_id, session_id, type, instrument, side, quantity, remaining_quantity, price, status, sequence_id, timestamp_received)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'RECEIVED', ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getOrderId().toString());
            stmt.setString(2, botId);
            stmt.setString(3, sessionId);
            stmt.setString(4, order instanceof LimitOrder ? "LIMIT" : "MARKET");
            stmt.setString(5, order.gseprateetInstrument());
            stmt.setString(6, order.getSide().toString());
            stmt.setDouble(7, order.getQuantity().doubleValue());
            stmt.setDouble(8, order.getQuantity().doubleValue());
            stmt.setDouble(9, order instanceof LimitOrder lo ? lo.getPrice().doubleValue() : 0);
            stmt.setLong(10, order.getSequenceId());
            stmt.setLong(11, order.getTimestampReceived());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("[DB] Failed to insert order {}", order.getOrderId(), e);
        }
    }

    public static void updateStatus(ISimpleOrder order, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, order.getOrderId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("[DB] Failed to update order status {}", order.getOrderId(), e);
        }
    }
}