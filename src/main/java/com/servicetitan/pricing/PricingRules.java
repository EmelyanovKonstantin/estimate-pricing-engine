package com.servicetitan.pricing;

import java.math.RoundingMode;
import java.util.Objects;

/**
 * Rates are stored as whole basis points (1 bp = 0.01%, 10 000 bp = 100%) so that
 * percentage math can be done entirely in integer arithmetic.
 */
public final class PricingRules {

    public static final int BASIS_POINTS_PER_UNIT = 10_000;

    private final int taxRateBasisPoints;
    private final int orderDiscountRateBasisPoints;
    private final RoundingMode roundingMode;

    public PricingRules(int taxRateBasisPoints, int orderDiscountRateBasisPoints, RoundingMode roundingMode) {
        if (taxRateBasisPoints < 0 || orderDiscountRateBasisPoints < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        this.taxRateBasisPoints = taxRateBasisPoints;
        this.orderDiscountRateBasisPoints = orderDiscountRateBasisPoints;
        this.roundingMode = Objects.requireNonNull(roundingMode);
    }

    /**
     * Convenience constructor taking fractional rates (0.0825 = 8.25%). The rate is converted
     * to basis points once, here; no floating-point value participates in money calculations.
     */
    public PricingRules(double taxRate, double orderDiscountRate, RoundingMode roundingMode) {
        this(toBasisPoints(taxRate), toBasisPoints(orderDiscountRate), roundingMode);
    }

    static int toBasisPoints(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        double scaled = rate * BASIS_POINTS_PER_UNIT;
        long basisPoints = Math.round(scaled);
        if (Math.abs(scaled - basisPoints) > 1e-6) {
            throw new IllegalArgumentException(
                    "Rate must be a whole number of basis points (multiple of 0.0001): " + rate);
        }
        return Math.toIntExact(basisPoints);
    }

    public int getTaxRateBasisPoints() {
        return taxRateBasisPoints;
    }

    public int getOrderDiscountRateBasisPoints() {
        return orderDiscountRateBasisPoints;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
}
