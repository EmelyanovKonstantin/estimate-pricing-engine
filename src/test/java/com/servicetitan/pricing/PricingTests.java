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
    void TaxIsChargedAfterDiscount() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        // 10000 -> 9000 after 10% discount; 8.25% of 9000 = 742.5 -> 743 half-up; 9000 + 743
        assertEquals(9743, calculator.calculateTotal(items, rules, MembershipTier.NONE));
    }

    @Test
    void goldTakesTenPercentOffLaborBeforeOrderDiscountAndTax() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        // 10000 -> 9000 (gold) -> 8100 (order discount) -> tax 668.25 -> 668 -> 8768
        // Gold applied after tax would give 9743 - 974 = 8769 instead.
        assertEquals(8768, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldDiscountIsRoundedBeforeOrderDiscountIsApplied() {
        List<LineItem> items = List.of(
                new LineItem(1005, LineItemType.LABOR, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.15, RoundingMode.HALF_UP);

        // gold first: 1005 - 101 = 904; 15% of 904 = 135.6 -> 136; 904 - 136 = 768
        // order discount first: 1005 - 151 = 854; 10% of 854 = 85.4 -> 85; 854 - 85 = 769
        assertEquals(768, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldDoesNotAffectPartsLines() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0, 0.0, RoundingMode.HALF_UP);

        assertEquals(10000, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void goldAppliesOnlyToLaborLineTotalsInMixedEstimate() {
        List<LineItem> items = List.of(
                new LineItem(5000, LineItemType.LABOR, 2),
                new LineItem(5000, LineItemType.PARTS, 1)
        );

        PricingRules rules = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);

        // labor 10000 -> 9000 (gold); parts 5000 unchanged; subtotal 14000
        // 14000 -> 12600 (order discount) -> tax 1039.5 -> 1040 -> 13640
        // Gold applied to parts as well would give 13152 instead.
        assertEquals(13640, calculator.calculateTotal(items, rules, MembershipTier.GOLD));
    }

    @Test
    void basisPointRulesProduceSameTotalAsFractionalRates() {
        List<LineItem> items = List.of(
                new LineItem(10000, LineItemType.LABOR, 1)
        );

        PricingRules fromFractions = new PricingRules(0.0825, 0.10, RoundingMode.HALF_UP);
        PricingRules fromBasisPoints = new PricingRules(825, 1000, RoundingMode.HALF_UP);

        assertEquals(825, fromFractions.getTaxRateBasisPoints());
        assertEquals(1000, fromFractions.getOrderDiscountRateBasisPoints());
        assertEquals(
                calculator.calculateTotal(items, fromFractions, MembershipTier.GOLD),
                calculator.calculateTotal(items, fromBasisPoints, MembershipTier.GOLD));
    }

    @Test
    void rejectsRatesFinerThanOneBasisPoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new PricingRules(0.08255, 0.0, RoundingMode.HALF_UP));
    }

    @Test
    void integerPercentageRoundsHalfUpOnExactHalfCent() {
        // 9000 * 825 / 10000 = 742.5 -> 743
        assertEquals(743, PricingCalculator.percentageOf(9000, 825, RoundingMode.HALF_UP));
        // 8100 * 825 / 10000 = 668.25 -> 668
        assertEquals(668, PricingCalculator.percentageOf(8100, 825, RoundingMode.HALF_UP));
        // 9999 * 1000 / 10000 = 999.9 -> 1000
        assertEquals(1000, PricingCalculator.percentageOf(9999, 1000, RoundingMode.HALF_UP));
        // exact result needs no rounding regardless of mode
        assertEquals(900, PricingCalculator.percentageOf(9000, 1000, RoundingMode.UNNECESSARY));
    }

    @Test
    void integerPercentageHonoursOtherRoundingModes() {
        assertEquals(742, PricingCalculator.percentageOf(9000, 825, RoundingMode.HALF_DOWN));
        assertEquals(742, PricingCalculator.percentageOf(9000, 825, RoundingMode.HALF_EVEN));
        assertEquals(742, PricingCalculator.percentageOf(9000, 825, RoundingMode.DOWN));
        assertEquals(743, PricingCalculator.percentageOf(9000, 825, RoundingMode.UP));
        assertThrows(ArithmeticException.class,
                () -> PricingCalculator.percentageOf(9000, 825, RoundingMode.UNNECESSARY));
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
