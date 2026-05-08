package com.tradecore.exchange.ordermanager;

public class InMemoryWallet implements Wallet {
    double currentAmount;
    double securedAmount;

    InMemoryWallet() {
        this.currentAmount = 0.0;
        this.securedAmount = 0.0;
    }

    InMemoryWallet(double currentAmount) {
        this.currentAmount = currentAmount;
        this.securedAmount = 0.0;
    }

    @Override
    public double getAmount() {
        return this.currentAmount;
    }

    @Override
    public void setAmount(double amount) {
        this.currentAmount = amount;
    }
    // Don't think about these

    @Override
    public double getHolding(String Symbol) {
        return 0;
    }

    @Override
    public void setHolding(String Symbol, double Amount) {}

    @Override
    public void setSecuredAmount(double amount) {}

    @Override
    public void revertSecuredAmount(double amount) {}

    @Override
    public void deductSecuredAmount(double amount) {}
}
