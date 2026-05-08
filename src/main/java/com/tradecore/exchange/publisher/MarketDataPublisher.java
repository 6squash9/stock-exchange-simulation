package com.tradecore.exchange.publisher;

import com.tradecore.exchange.order.ISimpleOrder;

public interface MarketDataPublisher {
    void publish(ISimpleOrder order, double price, int volume);
    void subscribe(MarketDataListener listener);
    void unsubscribe(MarketDataListener listener);
}
