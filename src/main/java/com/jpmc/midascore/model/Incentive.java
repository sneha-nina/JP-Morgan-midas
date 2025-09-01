package com.jpmc.midascore.model;

public class Incentive {
    private float amount; // if you use BigDecimal in your project, change to BigDecimal

    public Incentive() {}

    public Incentive(float amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
