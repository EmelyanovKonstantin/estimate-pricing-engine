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

        // Order of operations: membership (GOLD labor) -> order discount -> tax.
        long subtotal = lineItems.stream()
                .mapToLong(item -> lineTotalAfterMembership(item, membershipTier, rules.getRoundingMode()))
                .sum();

        long orderDiscount = percentageOf(
                subtotal,
                rules.getOrderDiscountRate(),
                rules.getRoundingMode());
        long discountedSubtotal = subtotal - orderDiscount;

        long tax = percentageOf(discountedSubtotal, rules.getTaxRate(), rules.getRoundingMode());

        return discountedSubtotal + tax;
    }

    private long lineTotalAfterMembership(
            LineItem item,
            MembershipTier membershipTier,
            RoundingMode roundingMode) {

        long lineTotal = item.totalInCents();

        if (membershipTier == MembershipTier.GOLD && item.getType() == LineItemType.LABOR) {
            return lineTotal - percentageOf(lineTotal, GOLD_LABOR_DISCOUNT, roundingMode);
        }

        return lineTotal;
    }

    private long percentageOf(long amountInCents, double rate, RoundingMode roundingMode) {
        return BigDecimal.valueOf(amountInCents)
                .multiply(BigDecimal.valueOf(rate))
                .setScale(0, roundingMode)
                .longValueExact();
    }
}
