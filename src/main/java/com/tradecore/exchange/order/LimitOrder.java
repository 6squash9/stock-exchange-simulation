package com.tradecore.exchange.order;
import java.util.UUID;
import java.math.BigDecimal;
public class LimitOrder implements ISimpleOrder {
    private final UUID orderId;
    private final String instrument;
    private BigDecimal quantity;
    private final BigDecimal price;
    private final Side side;
    private long sequenceId;
    private String botId;

    //timestamps
    private long timestampReceived;
    private long timestampValidated;
    private long timestampSequenced;
    private long timestampMatched;
    private long timestampPublished;

    public LimitOrder(String instrument, BigDecimal price, Side side, BigDecimal quantity) {
        this.orderId = UUID.randomUUID();
        this.instrument = instrument;
        this.price = price;
        this.side = side;
        this.quantity = quantity;
    }
    public BigDecimal getPrice() {
        return price;
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
        return "LimitOrder{" +
                "orderId=" + orderId +
                ", instrument='" + instrument + '\'' +
                ", side=" + side +
                ", quantity=" + quantity +
                ", price=" + price +
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
        return "";
    }

    @Override
    public void setBotId(String botId) {
        this.botId = botId;
    }
}
