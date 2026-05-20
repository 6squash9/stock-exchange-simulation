package com.tradecore.exchange.publisher;

import java.util.concurrent.CopyOnWriteArrayList;

import com.tradecore.exchange.metrics.LatencyManager;
import com.tradecore.exchange.metrics.LatencyRecord;
import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.persistence.LatencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMarketDataPublisher implements MarketDataPublisher {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMarketDataPublisher.class);
    private final CopyOnWriteArrayList<MarketDataListener> listeners = new CopyOnWriteArrayList<>(); //thread-safe and fast
    private volatile double currentPrice = 100.0;
    private volatile int lastVolume = 0;
    private volatile long lastUpdateTime = System.currentTimeMillis();
    private final LatencyManager latencyManager;
    private final String sessionId;

    //Constructor
    public SimpleMarketDataPublisher(LatencyManager latencyManager,String sessionId) {
        this.latencyManager = latencyManager;
        this.sessionId = sessionId;
    }

    @Override
    public void publish(ISimpleOrder order, double price, int volume) {
        //capture publish start time
        long publishStart = System.nanoTime();
        this.currentPrice = price;
        this.lastVolume = volume;
        this.lastUpdateTime = System.currentTimeMillis();

        //Notify all listeners(in memory)
        for(MarketDataListener listener: listeners){
            try {
                listener.onMarketData(price,volume);
            } catch (Exception e) {
                logger.error("[Publisher] Error notifying listener {}", listener.getClass().getSimpleName(), e);
            }
        }
        //capture publish end time
        long publishEnd = System.nanoTime();
        order.setTimestampPublished(publishEnd);

        if(latencyManager != null){
            LatencyRecord record = latencyManager.recordLatency(order);
            LatencyRepository.insert(record,sessionId);
        }
    }

    @Override
    public void subscribe(MarketDataListener listener) {
        if(listener!=null && !listeners.contains(listener)){
            listeners.add(listener);
            logger.info("[Publisher] Registered listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void unsubscribe(MarketDataListener listener) {
        boolean wasRemoved = listeners.remove(listener);
        if(wasRemoved){
            logger.info("[Publisher] Unregistered listener: {}", listener.getClass().getSimpleName());
        }
    }
    //getters
    public double getCurrentPrice() {
        return currentPrice;
    }

    public int getLastVolume() {
        return lastVolume;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getListenerCount() {
        return listeners.size();
    }
}
