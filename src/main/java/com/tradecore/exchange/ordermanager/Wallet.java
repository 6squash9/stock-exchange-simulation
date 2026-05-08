package com.tradecore.exchange.ordermanager;

public interface Wallet {
    double getAmount();
    void setAmount(double amount);

    // Don't think about these
    double getHolding(String Symbol);
    void setHolding(String Symbol, double Amount);
    void setSecuredAmount(double amount);
    void revertSecuredAmount(double amount);
    void deductSecuredAmount(double amount);

}

