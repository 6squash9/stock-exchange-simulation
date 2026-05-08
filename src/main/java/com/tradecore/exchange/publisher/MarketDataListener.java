package com.tradecore.exchange.publisher;

public interface MarketDataListener {
    void onMarketData(double price,int volume);
}
