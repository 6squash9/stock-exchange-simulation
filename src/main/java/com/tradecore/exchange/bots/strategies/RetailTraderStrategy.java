package com.tradecore.exchange.bots.strategies;

import com.tradecore.exchange.bots.TradingStrategy;
import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.order.LimitOrder;
import com.tradecore.exchange.order.MarketOrder;
import com.tradecore.exchange.order.Side;

import java.math.BigDecimal;
import java.util.Random;

public class RetailTraderStrategy implements TradingStrategy {
    private final Random random = new Random();

    @Override
    public ISimpleOrder makeDecision(double currentPrice, int lastVolume) {
        // Decide side based on price
        Side side;
        if (currentPrice > 110.0) {
            // High price - 70% chance to sell
            side = random.nextDouble() < 0.7 ? Side.SELL : Side.BUY;
        } else if (currentPrice < 90.0) {
            // Low price - 70% chance to buy
            side = random.nextDouble() < 0.7 ? Side.BUY : Side.SELL;
        } else {
            // Normal range - 50/50
            side = random.nextBoolean() ? Side.BUY : Side.SELL;
        }

        // Decide quantity (retail traders trade smaller sizes)
        BigDecimal quantity = BigDecimal.valueOf(1 + random.nextInt(10));

        // Decide order type (70% market, 30% limit)
        if (random.nextDouble() < 0.7) {
            // Market order
            return new MarketOrder("TICK", side, quantity);
        } else {
            // Limit order
            double priceVariation = (random.nextDouble() - 0.5) * 0.1; // ±5%
            BigDecimal limitPrice = BigDecimal.valueOf(currentPrice * (1 + priceVariation));
            return new LimitOrder("TICK", limitPrice, side, quantity);
        }
    }

    @Override
    public String getStrategyName() {
        return "RetailTrader";
    }
}
