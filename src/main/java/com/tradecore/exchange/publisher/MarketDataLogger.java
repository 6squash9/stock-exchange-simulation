package com.tradecore.exchange.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketDataLogger implements MarketDataListener{
    private static final Logger logger = LoggerFactory.getLogger(MarketDataLogger.class);

    @Override
    public void onMarketData(double price, int volume) {
        // Async Logging
        logger.info("TRADE | Price: {}, Volume: {}", String.format("%.2f", price), volume);
    }
}
