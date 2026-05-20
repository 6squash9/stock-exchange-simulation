package com.tradecore.exchange.metrics;

import com.tradecore.exchange.order.ISimpleOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LatencyManager {

    private static final Logger logger = LoggerFactory.getLogger(LatencyManager.class);
    //List to store all the Latencies
    private final List<LatencyRecord> latencyRecords = Collections.synchronizedList(new ArrayList<>());

    public LatencyRecord recordLatency(ISimpleOrder order){
        // Compute stage-wise latencies (convert ns → µs)
        long validationLatencyMicros = TimeUnit.NANOSECONDS.toMicros(
                order.getTimestampValidated() - order.getTimestampReceived());

        long sequencingLatencyMicros = TimeUnit.NANOSECONDS.toMicros(
                order.getTimestampSequenced() - order.getTimestampValidated());

        long matchingLatencyMicros = TimeUnit.NANOSECONDS.toMicros(
                order.getTimestampMatched() - order.getTimestampSequenced());

        long publishingLatencyMicros = TimeUnit.NANOSECONDS.toMicros(
                order.getTimestampPublished() - order.getTimestampMatched());

        long totalLatencyMicros = TimeUnit.NANOSECONDS.toMicros(
                order.getTimestampPublished() - order.getTimestampReceived());

        //create a latencyRecord object
        LatencyRecord record = new LatencyRecord(order.getOrderId(),validationLatencyMicros,sequencingLatencyMicros,matchingLatencyMicros,publishingLatencyMicros,totalLatencyMicros);
        //store it
        latencyRecords.add(record);

        return record;
    }

    public void printAverageLatency(){
        if (latencyRecords.isEmpty()) {
            logger.info("No latency data recorded yet.");
            return;
        }

        double avgValidation = latencyRecords.stream().mapToLong(LatencyRecord::getValidationLatencyMicros).average().orElse(0);
        double avgSequencing = latencyRecords.stream().mapToLong(LatencyRecord::getSequencingLatencyMicros).average().orElse(0);
        double avgMatching   = latencyRecords.stream().mapToLong(LatencyRecord::getMatchingLatencyMicros).average().orElse(0);
        double avgPublishing = latencyRecords.stream().mapToLong(LatencyRecord::getPublishingLatencyMicros).average().orElse(0);
        double avgTotal      = latencyRecords.stream().mapToLong(LatencyRecord::getTotalLatencyMicros).average().orElse(0);

        logger.info("\n===== STAGE-WISE AVERAGE LATENCIES =====");
        logger.info(String.format("Validation : %.2f µs (%.3f ms)", avgValidation, avgValidation / 1000.0));
        logger.info(String.format("Sequencing : %.2f µs (%.3f ms)", avgSequencing, avgSequencing / 1000.0));
        logger.info(String.format("Matching   : %.2f µs (%.3f ms)", avgMatching, avgMatching / 1000.0));
        logger.info(String.format("Publishing : %.2f µs (%.3f ms)", avgPublishing, avgPublishing / 1000.0));
        logger.info("---------------------------------------");
        logger.info(String.format("Total      : %.2f µs (%.3f ms)", avgTotal, avgTotal / 1000.0));
    }

    public void printTotalOrdersProcessed() {
        int count = latencyRecords.size();
        logger.info("\n===== ORDER STATS =====");
        logger.info("Total Orders Processed: {}", count);
    }

    public void printLatencyPercentiles() {
        if (latencyRecords.isEmpty()) {
            logger.info("No latency data recorded yet.");
            return;
        }

        List<Long> totals = new ArrayList<>(latencyRecords).stream()
                .map(LatencyRecord::getTotalLatencyMicros)
                .sorted()
                .toList();

        // compute percentile values (in microseconds)
        long p50 = percentile(totals, 50);
        long p95 = percentile(totals, 95);
        long p99 = percentile(totals, 99);

        logger.info("===== LATENCY PERCENTILES =====");
        logger.info(String.format("P50 (median): %d µs (%.3f ms)", p50, p50 / 1000.0));
        logger.info(String.format("P95: %d µs (%.3f ms)", p95, p95 / 1000.0));
        logger.info(String.format("P99: %d µs (%.3f ms)", p99, p99 / 1000.0));
    }

    private long percentile(List<Long> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        return sortedValues.get(Math.min(index, sortedValues.size() - 1));
    }

    public long getTotalOrdersProcessed(){
        return latencyRecords.size();
    }
}
