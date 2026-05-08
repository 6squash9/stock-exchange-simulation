package com.tradecore.exchange.bots;

import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.order.LimitOrder;
import com.tradecore.exchange.ordermanager.OrderManager;
import com.tradecore.exchange.publisher.MarketDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TradingBot implements MarketDataListener {
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);

    private final String botId;
    private final TradingStrategy strategy;
    private final OrderManager orderManager;

    private volatile double lastPrice = 100;
    private volatile int lastVolume = 0;

    private final AtomicInteger ordersSubmitted = new AtomicInteger(0);

    private volatile boolean running = false;
    private Thread executionThread;

    public TradingBot(String botId, TradingStrategy strategy, OrderManager orderManager){
        if (botId == null || botId.isBlank()) {
            throw new IllegalArgumentException("Bot ID cannot be null or empty");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (orderManager == null) {
            throw new IllegalArgumentException("Order manager cannot be null");
        }

        this.botId = botId;
        this.strategy = strategy;
        this.orderManager = orderManager;

        logger.info("[Bot: {}] Initialized with strategy: {}", botId, strategy.getStrategyName());
    }

    @Override
    public void onMarketData(double price, int volume) {
        // just update market state — DO NOT execute trade here
        this.lastPrice = price;
        this.lastVolume = volume;
    }

    private void executeTrade() {
        try {
            ISimpleOrder order = strategy.makeDecision(lastPrice, lastVolume);

            if (order == null) return;

            if (order instanceof LimitOrder lo && lo.getPrice() == null) {
                logger.error("🚨 BOT {} CREATED ORDER WITH NULL PRICE!", botId);
                return;
            }

            orderManager.submitOrder(order);
            ordersSubmitted.incrementAndGet();

            logger.debug("[Bot: {}] Submitted order: {} (Total: {})",
                    botId, order, ordersSubmitted);

        } catch (Exception e) {
            logger.error("[Bot: {}] Error executing trade", botId, e);
        }
    }

    public void start() {
        if (running) return;
        running = true;

        executionThread = new Thread(() -> {
            while (running) {
                executeTrade();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, botId + "-executor");

        executionThread.start();
        logger.info("[Bot: {}] Trading started", botId);
    }

    public void stop() {
        running = false;
        if (executionThread != null) {
            try {
                executionThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("[Bot: {}] Trading stopped. Total orders: {}", botId, ordersSubmitted.get());
    }

    public String getBotId() {
        return botId;
    }

    public String getStrategyName() {
        return strategy.getStrategyName();
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public int getLastVolume() {
        return lastVolume;
    }

    public int getOrdersSubmitted() {
        return ordersSubmitted.get();
    }

    @Override
    public String toString() {
        return String.format("TradingBot[id=%s, strategy=%s, orders=%d]",
                botId, strategy.getStrategyName(), ordersSubmitted.get());
    }
}
