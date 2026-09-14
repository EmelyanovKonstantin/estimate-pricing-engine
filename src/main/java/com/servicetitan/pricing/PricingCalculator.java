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
                .mapToLong(line -> lineTotalAfterMembershipDiscount(line, membershipTier, rules.getRoundingMode()))
                .sum();

        long orderDiscount = percentageOf(
                subtotal,
                rules.getOrderDiscountRate(),
                rules.getRoundingMode());
        long discountedSubtotal = subtotal - orderDiscount;

        long tax = percentageOf(discountedSubtotal, rules.getTaxRate(), rules.getRoundingMode());

        return discountedSubtotal + tax;
    }

    // GOLD discount is rounded per line, on the line total (amount * quantity).
    private long lineTotalAfterMembershipDiscount(
            LineItem line,
            MembershipTier membershipTier,
            RoundingMode roundingMode) {
        long lineTotal = line.totalInCents();
        if (membershipTier == MembershipTier.GOLD && line.getType() == LineItemType.LABOR) {
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
