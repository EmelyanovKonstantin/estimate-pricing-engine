package com.servicetitan.pricing;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void taxIsChargedAfterDiscount() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        // 10000 -> 9000 after 10% discount; 9000 * 0.0825 = 742.5 -> 743; 9000 + 743
        assertEquals(9743, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void respectsRoundingModeOnTax() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_DOWN);

        // same as above, but 742.5 rounds down to 742
        assertEquals(9742, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void goldDiscountsLaborLinesOnly() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1),
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        // LABOR 10000 -> 9000, PARTS untouched
        assertEquals(19000, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldAppliesToLineTotalWithQuantity() {
        List<LineItem> items = List.of(
                new LineItem(1500, LineItemType.LABOR, 2)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        // 1500 * 2 = 3000 -> 10% off = 2700
        assertEquals(2700, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldAppliedBeforeOrderDiscount() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.10, RoundingMode.HALF_UP);

        // 10000 -> 9000 (gold) -> 8100 (order discount on the reduced subtotal), not 8000
        assertEquals(8100, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldThenOrderDiscountThenTax() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1),
                new LineItem(5000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        // gold: 10000 -> 9000; subtotal 14000; order discount 1400 -> 12600;
        // tax 12600 * 0.0825 = 1039.5 -> 1040; total 13640
        assertEquals(13640, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void noneTierLeavesLaborUnchanged() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        assertEquals(10000, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void goldRoundsPerLine() {
        List<LineItem> items = List.of(
                new LineItem(1005, LineItemType.LABOR, 1),
                new LineItem(1005, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        // per line: 100.5 -> 101 each, 2010 - 202 = 1808 (aggregate rounding would give 1809)
        assertEquals(1808, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void rejectsNullMembershipTier() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        assertThrows(NullPointerException.class, () -> calculator.calculateTotal(items, rules, null));
    }

    @Test
    void emptyLineItemsTotalZero() {
        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        assertEquals(0, calculator.calculateTotal(List.of(), rules, MembershipTier.GOLD));
    }
}
