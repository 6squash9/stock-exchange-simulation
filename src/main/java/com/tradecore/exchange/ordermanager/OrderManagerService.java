package com.tradecore.exchange.ordermanager;

import com.tradecore.exchange.order.ISimpleOrder;
import com.tradecore.exchange.sequencer.Sequencer;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrderManagerService implements OrderManager {
    private static final Logger logger = LoggerFactory.getLogger(OrderManagerService.class);
    private final Sequencer sequencer;
    private final AtomicLong sequenceCounter = new AtomicLong(0);

    public OrderManagerService(Sequencer sequencer) {
        this.sequencer = sequencer;
        logger.info("[OrderManager] Initialized");
    }

    @Override
    public void submitOrder(ISimpleOrder order) {
        // capture order received time
        long timeReceived = System.nanoTime();
        order.setTimestampReceived(timeReceived);
        //assign sequence id's immediately
        long sequenceId = sequenceCounter.incrementAndGet(); //unique sequence ID
        order.setSequenceId(sequenceId);
        logger.info("[OrderManager] Order {} arrived → assigned sequence ID: {}", order.getOrderId(), sequenceId);
        validateOrder(order);
    }

    @Override
    public void validateOrder(ISimpleOrder order) {
        try {
        boolean check = RiskChecker.check(order);
                if(check) {
                    // capture validation time
                    long timeValidated = System.nanoTime();
                    order.setTimestampValidated(timeValidated);

                    sequencer.enqueueOrder(order); //send it to the sequencer
                    logger.info("[OrderManager] Order {} (seq: {}) → validated", order.getOrderId(), order.getSequenceId());
                }
                else {
                    logger.warn("[OrderManager] Order {} (seq: {}) → rejected", order.getOrderId(), order.getSequenceId());
                }

        } catch (Exception e) {
            logger.error("[OrderManager] Validation error for order {}: {}", order.getOrderId(), e.getMessage(), e);
        }

    }
}
