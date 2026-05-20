package com.tradecore.exchange.order;
import java.math.BigDecimal;
import java.util.UUID;
public class MarketOrder implements ISimpleOrder{
    private final UUID orderId;
    private final String instrument;
    private BigDecimal quantity;
    private final Side side;
    private long sequenceId;
    private String botId;

    //timestamps
    private long timestampReceived;
    private long timestampValidated;
    private long timestampSequenced;
    private long timestampMatched;
    private long timestampPublished;

    public MarketOrder(String instrument, Side side, BigDecimal quantity) {
        this.orderId = UUID.randomUUID();
        this.instrument = instrument;
        this.quantity = quantity;
        this.side = side;
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }

    @Override
    public String gseprateetInstrument() {
        return instrument;
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "MarketOrder{" +
                "orderId=" + orderId +
                ", instrument='" + instrument + '\'' +
                ", side=" + side +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public long getSequenceId() {
        return sequenceId;
    }

    @Override
    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public void setTimestampReceived(long timestamp) {
        this.timestampReceived = timestamp;
    }

    @Override
    public long getTimestampReceived() {
        return timestampReceived;
    }

    @Override
    public void setTimestampValidated(long timestamp) {
        this.timestampValidated = timestamp;
    }

    @Override
    public long getTimestampValidated() {
        return timestampValidated;
    }

    @Override
    public void setTimestampSequenced(long timestamp) {
        this.timestampSequenced = timestamp;
    }

    @Override
    public long getTimestampSequenced() {
        return timestampSequenced;
    }

    @Override
    public void setTimestampMatched(long timestamp) {
        this.timestampMatched = timestamp;
    }

    @Override
    public long getTimestampMatched() {
        return timestampMatched;
    }

    @Override
    public void setTimestampPublished(long timestamp) {
        this.timestampPublished = timestamp;
    }

    @Override
    public long getTimestampPublished() {
        return timestampPublished;
    }

    @Override
    public String getBotId() {
        return botId;
    }
    @Override
     public void setBotId(String botId) {
        this.botId = botId;
    }
}
