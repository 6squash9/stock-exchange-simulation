package com.tradecore.exchange.order;
import java.math.BigDecimal;
import java.util.UUID;


/**
 * An interface representing a generic order.
 * This provides a contract for different order types like Limit and Market orders.
 */
public interface ISimpleOrder {
    /**
     * Gets the unique identifier for the order.
     */
    UUID getOrderId();

    /**
     * Gets the financial instrument (e.g., "AAPL", "GOOG").
     */
    String gseprateetInstrument();

    /**
     * Gets the quantity of the instrument to be traded.
     */
    BigDecimal getQuantity();

    /**
     * Gets the side of the order (BUY or SELL).
     */
    Side getSide();

    /**
     * Sets the remaining quantity for the order. This is used when an order is partially filled.
     * @param quantity The new remaining quantity.
     */
    void setQuantity(BigDecimal quantity);

    void setSequenceId(long sequenceId);

    long getSequenceId();

    // latency measurement methods
    /**
     * Sets the timestamp when OrderManager receives the order (nanoseconds)
     */
    void setTimestampReceived(long timestamp);

    /**
     * Gets the timestamp when OrderManager received the order
     */
    long getTimestampReceived();

    /**
     * Sets the timestamp after order validation (nanoseconds)
     */
    void setTimestampValidated(long timestamp);

    /**
     * Gets the timestamp after validation
     */
    long getTimestampValidated();

    /**
     * Sets the timestamp after sequencer processes the order (nanoseconds)
     */
    void setTimestampSequenced(long timestamp);

    /**
     * Gets the timestamp after sequencing
     */
    long getTimestampSequenced();

    /**
     * Sets the timestamp after matching engine executes the order (nanoseconds)
     */
    void setTimestampMatched(long timestamp);

    /**
     * Gets the timestamp after matching
     */
    long getTimestampMatched();

    void setTimestampPublished(long timestamp);

    long getTimestampPublished();
}
