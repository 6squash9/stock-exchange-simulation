package com.tradecore.exchange.metrics;

import java.util.UUID;

public class LatencyRecord {
    private final UUID orderId;
    private final long validationLatencyMicros;
    private final long sequencingLatencyMicros;
    private final long matchingLatencyMicros;
    private final long publishingLatencyMicros;
    private final long totalLatencyMicros;

    public LatencyRecord(UUID orderId, long validationLatencyMicros, long sequencingLatencyMicros,
                         long matchingLatencyMicros, long publishingLatencyMicros, long totalLatencyMicros) {
        this.orderId = orderId;
        this.validationLatencyMicros = validationLatencyMicros;
        this.sequencingLatencyMicros = sequencingLatencyMicros;
        this.matchingLatencyMicros = matchingLatencyMicros;
        this.publishingLatencyMicros = publishingLatencyMicros;
        this.totalLatencyMicros = totalLatencyMicros;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public long getValidationLatencyMicros() {
        return validationLatencyMicros;
    }

    public long getSequencingLatencyMicros() {
        return sequencingLatencyMicros;
    }

    public long getMatchingLatencyMicros() {
        return matchingLatencyMicros;
    }

    public long getPublishingLatencyMicros() {
        return publishingLatencyMicros;
    }

    public long getTotalLatencyMicros() {
        return totalLatencyMicros;
    }

    @Override
    public String toString() {
        return "LatencyRecord{" +
                "orderId='" + orderId + '\'' +
                ", validationLatencyMicros=" + validationLatencyMicros + "µs" +
                ", sequencingLatencyMicros=" + sequencingLatencyMicros + "µs" +
                ", matchingLatencyMicros=" + matchingLatencyMicros + "µs" +
                ", publishingLatencyMicros=" + publishingLatencyMicros + "µs" +
                ", totalLatencyMicros=" + totalLatencyMicros + "µs" +
                '}';
    }
}
