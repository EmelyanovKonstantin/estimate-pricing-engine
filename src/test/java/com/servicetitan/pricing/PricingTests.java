package com.servicetitan.pricing;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingTests {

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void calculatesSubtotalForMultipleLinesAndQuantities() {
        List<LineItem> items = List.of(
                new LineItem(1500, LineItemType.LABOR, 2),
                new LineItem(2000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        assertEquals(5000, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void appliesOrderDiscountWhenThereIsNoTax() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.10, RoundingMode.HALF_UP);

        assertEquals(9000, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void appliesTaxWhenThereIsNoDiscount() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.0, RoundingMode.HALF_UP);

        assertEquals(10825, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void zeroRatesLeaveSubtotalUnchanged() {
        List<LineItem> items = List.of(
                new LineItem(1234, LineItemType.LABOR, 1),
                new LineItem(567, LineItemType.PARTS, 2)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        assertEquals(2368, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }
}
