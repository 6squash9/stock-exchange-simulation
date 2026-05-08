package com.tradecore.exchange.bots;

import com.tradecore.exchange.order.ISimpleOrder;

public interface TradingStrategy {
    ISimpleOrder makeDecision(double currentPrice,int lastVolume);
    String getStrategyName();
}
