    package com.tradecore.exchange.matchingengine;
    import com.tradecore.exchange.order.ISimpleOrder;

    import java.util.UUID;

    public interface ISimpleMatchingEngine {
        void AddOrder(ISimpleOrder order);
        void UpdateOrder(UUID orderToUpdate, ISimpleOrder newOrder);
        void CancelOrder(UUID orderToCancel);
    }
