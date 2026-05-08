package com.tradecore.exchange.matchingengine;

import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.order.LimitOrder;
import com.tradecore.exchange.order.MarketOrder;
import com.tradecore.exchange.order.Side;
import com.tradecore.exchange.publisher.MarketDataPublisher;
import com.tradecore.exchange.visualizer.GraphVisualizer;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchingEngine implements ISimpleMatchingEngine, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MatchingEngine.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private BigDecimal lastPrice = new BigDecimal(100);
    private final Thread engineThread;
    private final Queue<ISimpleOrder> incomingQueue = new ConcurrentLinkedQueue<>();
    private final PriorityQueue<LimitOrder> bids = new PriorityQueue<>(Comparator.comparing(LimitOrder::getPrice).reversed());
    private final PriorityQueue<LimitOrder> asks = new PriorityQueue<>(Comparator.comparing(LimitOrder::getPrice));
    private final Deque<MarketOrder> marketBuy = new ArrayDeque<>();
    private final Deque<MarketOrder> marketSell = new ArrayDeque<>();
    private final MarketDataPublisher publisher;


    public MatchingEngine(MarketDataPublisher publisher) {
        this.publisher = publisher;
        this.engineThread = new Thread(this, "matching-engine-thread");
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            engineThread.start();
            logger.info("Matching Engine Started.");
        }
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void AddOrder(ISimpleOrder order) {
        logger.info("[MatchingEngine] Received order {}", order.getOrderId());
        incomingQueue.add(order);
    }

    private void processNewOrder(ISimpleOrder order){
        if (order instanceof LimitOrder) {
            if (order.getSide() == Side.BUY) {
                    bids.add((LimitOrder) order);
            } else {
                    asks.add((LimitOrder) order);
            }
        } else {
            if (order.getSide() == Side.BUY) {
                    marketBuy.add((MarketOrder) order);
            } else {
                    marketSell.add((MarketOrder) order);
            }
        }

    }

    @Override
    public void run() {
        while (running.get()) {
            ISimpleOrder newOrder;
            while((newOrder = incomingQueue.poll()) != null){
                processNewOrder(newOrder);
            }

            matchMarketBuy();
            matchMarketSell();
            matchLimitOrder();
            try {
                // Sleep briefly to prevent busy-waiting
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Matching Engine Stopped.");
    }

    private void matchMarketBuy() {
        while (!marketBuy.isEmpty() && !asks.isEmpty()) {
            MarketOrder buyOrder = marketBuy.peekFirst();
            LimitOrder sellOrder = asks.peek();

            if (buyOrder == null || sellOrder == null) break;
            BigDecimal tradedQuantity = buyOrder.getQuantity().min(sellOrder.getQuantity());
            logger.info("\uD83D\uDD25 TRADE EXECUTED --- Matched BUY quantity of {} at price {}", tradedQuantity, sellOrder.getPrice());

            //Capture time on trade executed
            long timeMatched = System.nanoTime();
            buyOrder.setTimestampMatched(timeMatched);
            sellOrder.setTimestampMatched(timeMatched);

            lastPrice = sellOrder.getPrice();
            GraphVisualizer.updateStockPrice(lastPrice);
            // Debug
            System.out.println("✅ Trade at: " + lastPrice + " | Qty: " + tradedQuantity);
            publisher.publish(buyOrder,lastPrice.doubleValue(), tradedQuantity.intValue());
            if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) > 0) {
                buyOrder.setQuantity(buyOrder.getQuantity().subtract(tradedQuantity));
                asks.poll();
            } else if (sellOrder.getQuantity().compareTo(buyOrder.getQuantity()) > 0) {
                sellOrder.setQuantity(sellOrder.getQuantity().subtract(tradedQuantity));
                marketBuy.pollFirst();
            } else {
                marketBuy.pollFirst();
                asks.poll();
            }
        }
    }

    private void matchMarketSell() {
        while (!bids.isEmpty() && !marketSell.isEmpty()) {
            LimitOrder buyOrder = bids.peek();
            MarketOrder sellOrder = marketSell.peekFirst();
            if (buyOrder == null || sellOrder == null) break;

            BigDecimal tradedQuantity = buyOrder.getQuantity().min(sellOrder.getQuantity());
            logger.info("TRADE EXECUTED --- Matched SELL quantity of {} at price {}", tradedQuantity, buyOrder.getPrice());

            //capture time on trade executed
            long timeMatched = System.nanoTime();
            buyOrder.setTimestampMatched(timeMatched);
            sellOrder.setTimestampMatched(timeMatched);

            lastPrice = buyOrder.getPrice();
            GraphVisualizer.updateStockPrice(lastPrice);

            publisher.publish(sellOrder,lastPrice.doubleValue(),tradedQuantity.intValue());

            if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) > 0) {
                buyOrder.setQuantity(buyOrder.getQuantity().subtract(tradedQuantity));
                marketSell.pollFirst();
            } else if (sellOrder.getQuantity().compareTo(buyOrder.getQuantity()) > 0) {
                sellOrder.setQuantity(sellOrder.getQuantity().subtract(tradedQuantity));
                bids.poll();
            } else {
                marketSell.pollFirst();
                bids.poll();
            }
        }
    }

    private void matchLimitOrder() {
        while (!asks.isEmpty() && !bids.isEmpty()) {
                    LimitOrder buyOrder = bids.peek();
                    LimitOrder sellOrder = asks.peek();

                    if (buyOrder == null || sellOrder == null) break;
                    if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) < 0) break;
                    BigDecimal tradedQuantity = buyOrder.getQuantity().min(sellOrder.getQuantity());
                    BigDecimal executionPrice = sellOrder.getPrice();
                    logger.info("\uD83D\uDD25 TRADE EXECUTED --- Matched LIMIT quantity of {} at price {}", tradedQuantity, executionPrice);

                    //capture time on trade executed
                    long timeMatched = System.nanoTime();
                    buyOrder.setTimestampMatched(timeMatched);
                    sellOrder.setTimestampMatched(timeMatched);

                    lastPrice = executionPrice;
                    GraphVisualizer.updateStockPrice(lastPrice);

                    publisher.publish(buyOrder, lastPrice.doubleValue(), tradedQuantity.intValue());

                    if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) > 0) {
                        buyOrder.setQuantity(buyOrder.getQuantity().subtract(tradedQuantity));
                        asks.poll();
                    } else if (sellOrder.getQuantity().compareTo(buyOrder.getQuantity()) > 0) {
                        sellOrder.setQuantity(sellOrder.getQuantity().subtract(tradedQuantity));
                        bids.poll();
                    } else {
                        bids.poll();
                        asks.poll();
                    }
                }
    }

    public BigDecimal getTick() {
        return lastPrice;
    }

    public int getQueueSize(){
            return bids.size() + asks.size() + marketBuy.size() + marketSell.size();
    }
    public int getIncomingQueueSize(){
        return incomingQueue.size();
    }
    public AtomicBoolean isRunning(){
        return running;
    }
    public int getBidsSize() {
            return bids.size();
    }

    public int getAsksSize() {
            return asks.size();
    }

    public int getMarketBuySize() {
            return marketBuy.size();
    }

    public int getMarketSellSize() {
            return marketSell.size();
    }
    @Override
    public void UpdateOrder(UUID orderToUpdate, ISimpleOrder newOrder) {
        // Implementation for updating an order would go here.
    }

    @Override
    public void CancelOrder(UUID orderToCancel) {
        // Implementation for canceling an order would go here.
    }
}
