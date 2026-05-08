package com.tradecore.exchange.ordermanager;

import com.tradecore.exchange.order.ISimpleOrder;

import java.math.BigDecimal;

public class RiskChecker {

    public static boolean check(ISimpleOrder order) {
        return order.getQuantity().compareTo(new BigDecimal(0)) > 0;
    }

    public static boolean check(ISimpleOrder order, Wallet wallet) {
        return true; // TODO: Will change this soon
    }
}
