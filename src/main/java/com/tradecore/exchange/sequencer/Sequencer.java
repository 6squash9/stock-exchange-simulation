package com.tradecore.exchange.sequencer;

import com.tradecore.exchange.order.ISimpleOrder;

public interface Sequencer {
    void enqueueOrder(ISimpleOrder order);
    void start();
    void stop();
}
