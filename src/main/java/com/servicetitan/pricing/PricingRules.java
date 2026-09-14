package com.servicetitan.pricing;

import java.math.RoundingMode;

public final class PricingRules {
    private final double taxRate;
    private final double orderDiscountRate;
    private final RoundingMode roundingMode;

    public PricingRules(double taxRate, double orderDiscountRate, RoundingMode roundingMode) {
        if (taxRate < 0 || orderDiscountRate < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        this.taxRate = taxRate;
        this.orderDiscountRate = orderDiscountRate;
        this.roundingMode = roundingMode;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double getOrderDiscountRate() {
        return orderDiscountRate;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
}
