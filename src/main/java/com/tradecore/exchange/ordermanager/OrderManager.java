package com.tradecore.exchange.ordermanager;

import com.tradecore.exchange.order.ISimpleOrder;

public interface OrderManager {
    void submitOrder(ISimpleOrder order);
    void validateOrder(ISimpleOrder order); // TODO: Changed this to void, in case this causes some issue, rervert it back to boolean
}
