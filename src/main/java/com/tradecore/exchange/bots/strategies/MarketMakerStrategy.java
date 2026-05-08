package com.tradecore.exchange.bots.strategies;

import com.tradecore.exchange.bots.TradingStrategy;
import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.order.LimitOrder;
import com.tradecore.exchange.order.MarketOrder;
import com.tradecore.exchange.order.Side;

import java.math.BigDecimal;
import java.util.Random;

public class MarketMakerStrategy implements TradingStrategy {
    private final Random random = new Random();
    @Override
    public ISimpleOrder makeDecision(double currentPrice, int lastVolume) {
        // Decide side based on price
        Side side;
        if (currentPrice > 105.0) {
            // High price - 60% chance to sell
            side = random.nextDouble() < 0.6 ? Side.SELL : Side.BUY;
        } else if (currentPrice < 95.0) {
            // Low price - 60% chance to buy
            side = random.nextDouble() < 0.6 ? Side.BUY : Side.SELL;
        } else {
            // Normal range - 50/50
            side = random.nextBoolean() ? Side.BUY : Side.SELL;
        }

        // Decide quantity (market makers trade larger sizes)
        BigDecimal quantity = BigDecimal.valueOf(100 + random.nextInt(401));

        // Decide order type (80% limit, 20% market)
        if (random.nextDouble() < 0.8) {
            // Limit order
            double priceVariation = (random.nextDouble() - 0.5) * 0.06; // ±3%
            BigDecimal limitPrice = BigDecimal.valueOf(currentPrice * (1 + priceVariation));
            return new LimitOrder("TICK", limitPrice, side, quantity);
        } else {
            // Market order
            return new MarketOrder("TICK", side, quantity);
        }
    }

    @Override
    public String getStrategyName() {
        return "MarketMaker";
    }
}
