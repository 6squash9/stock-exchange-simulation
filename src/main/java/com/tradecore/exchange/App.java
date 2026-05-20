package com.tradecore.exchange;

import com.tradecore.exchange.bots.TradingBot;
import com.tradecore.exchange.bots.strategies.MarketMakerStrategy;
import com.tradecore.exchange.bots.strategies.RetailTraderStrategy;
import com.tradecore.exchange.matchingengine.MatchingEngine;
import com.tradecore.exchange.metrics.LatencyManager;
import com.tradecore.exchange.ordermanager.OrderManagerService;
import com.tradecore.exchange.persistence.DatabaseManager;
import com.tradecore.exchange.persistence.SessionRepository;
import com.tradecore.exchange.publisher.MarketDataLogger;
import com.tradecore.exchange.publisher.SimpleMarketDataPublisher;
import com.tradecore.exchange.sequencer.SequencerService;
import com.tradecore.exchange.visualizer.GraphVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;


public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.initialize();
        String sessionId = UUID.randomUUID().toString();
        SessionRepository.insert(sessionId,"SYNCHRONIZED");

        GraphVisualizer.start();
        logger.info("🚀 Starting Exchange\n");
        //
        LatencyManager latencyManager = new LatencyManager();
        SimpleMarketDataPublisher publisher = new SimpleMarketDataPublisher(latencyManager,sessionId);
        MatchingEngine matchingEngine = new MatchingEngine(publisher,sessionId);
        matchingEngine.start();

        publisher.subscribe(new MarketDataLogger());

        SequencerService sequencer = new SequencerService(matchingEngine);
        OrderManagerService orderManager = new OrderManagerService(sequencer,sessionId);
        sequencer.start();

        // Create bots
        TradingBot mmBot = new TradingBot("MM-1", new MarketMakerStrategy(), orderManager);
        TradingBot rtBot = new TradingBot("RT-1", new RetailTraderStrategy(), orderManager);

        // Subscribe to publisher
        publisher.subscribe(mmBot);
        publisher.subscribe(rtBot);

        // Start bot trading
        mmBot.start();
        rtBot.start();

        logger.info("✅ Bots running\n");

        // Run for x seconds
        int runtimeMillis = 5000;
        try {
            logger.info("Running simulation for {} seconds...", runtimeMillis / 1000);
            Thread.sleep(runtimeMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Shutdown
        mmBot.stop();
        rtBot.stop();

        for (int i = 5; i > 0; i--) {
            logger.info("⏳ Time remaining: {}s | Orders Processed: {}",
                    i, latencyManager.getTotalOrdersProcessed());
            try { Thread.sleep(1000); } catch (Exception e) {}
        }
        // --- Print latency metrics here ---
        logger.info("\n===== PERFORMANCE METRICS =====");
        latencyManager.printTotalOrdersProcessed();
        latencyManager.printAverageLatency();
        latencyManager.printLatencyPercentiles();

        logger.info("📊 Final Stats:");
        logger.info("  {}", mmBot);
        logger.info("  {}", rtBot);

        // benchmark
        double runtimeSeconds = runtimeMillis / 1000.0;
        long totalOrdersProcessed = latencyManager.getTotalOrdersProcessed();
        long totalOrdersSubmitted = mmBot.getOrdersSubmitted() + rtBot.getOrdersSubmitted();
        double throughput = totalOrdersProcessed / runtimeSeconds;
        logger.info("\n===== BENCHMARK REPORT =====");
        logger.info("Runtime (s): {}", String.format("%.2f", runtimeSeconds));
        logger.info("Total Orders Submitted: {}",totalOrdersSubmitted);
        logger.info("Total Orders Processed: {}", totalOrdersProcessed);
        logger.info("Throughput (orders/sec): {}", String.format("%.2f", throughput));

        logger.info("\n✅ Done\n");

        // Persist session results
        SessionRepository.update(sessionId, runtimeSeconds, totalOrdersSubmitted, totalOrdersProcessed, throughput);
        System.exit(0);

    }
}
