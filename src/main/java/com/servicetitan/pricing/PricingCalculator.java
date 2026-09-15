package com.servicetitan.pricing;

import java.math.RoundingMode;
import java.util.List;

import static com.servicetitan.pricing.PricingRules.BASIS_POINTS_PER_UNIT;

public class PricingCalculator {

    private static final int GOLD_LABOR_DISCOUNT_BASIS_POINTS = 1_000; // 10%

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
                rules.getOrderDiscountRateBasisPoints(),
                rules.getRoundingMode());
        long discountedSubtotal = subtotal - orderDiscount;

        long tax = percentageOf(discountedSubtotal, rules.getTaxRateBasisPoints(), rules.getRoundingMode());

        return discountedSubtotal + tax;
    }

    private long lineTotalAfterMembership(
            LineItem item,
            MembershipTier membershipTier,
            RoundingMode roundingMode) {

        long lineTotal = item.totalInCents();

        if (membershipTier == MembershipTier.GOLD && item.getType() == LineItemType.LABOR) {
            return lineTotal - percentageOf(lineTotal, GOLD_LABOR_DISCOUNT_BASIS_POINTS, roundingMode);
        }

        return lineTotal;
    }

    /**
     * amount * rate, entirely in integer arithmetic: the exact product is held as
     * (amount * basisPoints) / 10 000 and rounded once via the division remainder.
     */
    static long percentageOf(long amountInCents, int rateBasisPoints, RoundingMode roundingMode) {
        long numerator = Math.multiplyExact(amountInCents, (long) rateBasisPoints);
        return divideAndRound(numerator, BASIS_POINTS_PER_UNIT, roundingMode);
    }

    /**
     * Integer division with rounding for non-negative numerator and positive denominator.
     * Money never leaves {@code long}; the fractional part is inspected only as a remainder.
     */
    static long divideAndRound(long numerator, long denominator, RoundingMode roundingMode) {
        if (numerator < 0 || denominator <= 0) {
            throw new IllegalArgumentException("numerator must be >= 0 and denominator > 0");
        }

        long quotient = numerator / denominator;
        long remainder = numerator % denominator;
        if (remainder == 0) {
            return quotient;
        }

        long twiceRemainder = remainder * 2;
        return switch (roundingMode) {
            case DOWN, FLOOR -> quotient;
            case UP, CEILING -> quotient + 1;
            case HALF_UP -> twiceRemainder >= denominator ? quotient + 1 : quotient;
            case HALF_DOWN -> twiceRemainder > denominator ? quotient + 1 : quotient;
            case HALF_EVEN -> {
                if (twiceRemainder > denominator) {
                    yield quotient + 1;
                }
                if (twiceRemainder < denominator) {
                    yield quotient;
                }
                yield (quotient % 2 == 0) ? quotient : quotient + 1;
            }
            case UNNECESSARY -> throw new ArithmeticException(
                    "Rounding necessary: " + numerator + " / " + denominator);
        };
    }
}
