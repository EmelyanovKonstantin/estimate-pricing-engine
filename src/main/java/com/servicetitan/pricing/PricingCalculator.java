package com.servicetitan.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PricingCalculator {

    private static final double GOLD_LABOR_DISCOUNT = 0.10d;

    public long calculateTotal(
            List<LineItem> lineItems,
            PricingRules rules,
            MembershipTier membershipTier) {

        long subtotal = lineItems.stream()
                .mapToLong(LineItem::totalInCents)
                .sum();

        // TODO: Fix the order of operations and add GOLD membership pricing.
        // The current implementation calculates tax before the order discount.
        long tax = percentageOf(subtotal, rules.getTaxRate(), rules.getRoundingMode());
        long orderDiscount = percentageOf(
                subtotal,
                rules.getOrderDiscountRate(),
                rules.getRoundingMode());

        return subtotal + tax - orderDiscount;
    }

    private long percentageOf(long amountInCents, double rate, RoundingMode roundingMode) {
        return BigDecimal.valueOf(amountInCents)
                .multiply(BigDecimal.valueOf(rate))
                .setScale(0, roundingMode)
                .longValueExact();
    }
}
