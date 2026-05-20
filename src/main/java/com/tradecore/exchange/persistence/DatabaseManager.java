package com.tradecore.exchange.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    //database connection url
    private static final String DB_URL = "jdbc:sqlite:tradecore.db"; //tradecore.db is the file name
    public static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    //returns a db connection object
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        //try with resources
        try (Connection conn = getConnection(); //throws SQLException
             Statement stmt = conn.createStatement()) {
            //parent table first , child after
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sessions (
                    session_id TEXT PRIMARY KEY,
                    engine_version TEXT NOT NULL,
                    runtime_seconds REAL,
                    total_orders_submitted INTEGER,
                    total_orders_processed INTEGER,
                    throughput REAL,
                    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            //orders table depends on session
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_id TEXT PRIMARY KEY,
                    bot_id TEXT NOT NULL,
                    session_id TEXT NOT NULL REFERENCES sessions(session_id),
                    type TEXT NOT NULL,
                    instrument TEXT NOT NULL,
                    side TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    remaining_quantity REAL NOT NULL,
                    price REAL,
                    status TEXT NOT NULL DEFAULT 'RECEIVED',
                    sequence_id INTEGER,
                    timestamp_received INTEGER,
                    timestamp_validated INTEGER,
                    timestamp_sequenced INTEGER,
                    timestamp_matched INTEGER,
                    timestamp_published INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            //trades table depends on both order and session
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS trades (
                    trade_id TEXT PRIMARY KEY,
                    buy_order_id TEXT NOT NULL REFERENCES orders(order_id),
                    sell_order_id TEXT NOT NULL REFERENCES orders(order_id),
                    instrument TEXT NOT NULL,
                    match_type TEXT NOT NULL,
                    price REAL NOT NULL,
                    quantity REAL NOT NULL,
                    session_id TEXT NOT NULL REFERENCES sessions(session_id),
                    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            //latency metrics depends on both orders and session
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS latency_metrics (
                    metric_id TEXT PRIMARY KEY,
                    order_id TEXT NOT NULL REFERENCES orders(order_id),
                    session_id TEXT NOT NULL REFERENCES sessions(session_id),
                    validation_latency_us INTEGER NOT NULL,
                    sequencing_latency_us INTEGER NOT NULL,
                    matching_latency_us INTEGER NOT NULL,
                    publishing_latency_us INTEGER NOT NULL,
                    total_latency_us INTEGER NOT NULL,
                    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            logger.info("[DB] Tables initialized successfully");

        } catch (SQLException e) {
            //if tables can't be created on startup, the app is broken and should crash immediately
            logger.error("[DB] Failed to initialize tables", e);
            throw new RuntimeException(e);
        }
    }
}
