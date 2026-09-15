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

        // TODO: add GOLD membership pricing (applied before the order discount).
        long orderDiscount = percentageOf(
                subtotal,
                rules.getOrderDiscountRate(),
                rules.getRoundingMode());
        long discountedSubtotal = subtotal - orderDiscount;

        long tax = percentageOf(discountedSubtotal, rules.getTaxRate(), rules.getRoundingMode());

        return discountedSubtotal + tax;
    }

    private long percentageOf(long amountInCents, double rate, RoundingMode roundingMode) {
        return BigDecimal.valueOf(amountInCents)
                .multiply(BigDecimal.valueOf(rate))
                .setScale(0, roundingMode)
                .longValueExact();
    }
}
