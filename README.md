# Estimate Pricing Engine

## Context

Estimates drive revenue. When a technician builds an estimate, the pricing
engine turns a set of line items into a grand total the customer sees and pays.

Money is represented as integer cents. Percentage calculations may produce
fractional cents, so the configured rounding policy must be respected.

The pricing rule is split between `PricingRules` and `PricingCalculator`:

- `PricingRules` holds the tax rate, order discount rate, and rounding policy.
- `PricingCalculator` combines line items with those rules to produce the total.

## Bug to fix

Tax is currently charged on the pre-discount subtotal, and the order discount
is subtracted after tax. This reverses the required order of operations.

Concrete example:

- one $100.00 line = 10000 cents
- order discount = 10%
- tax = 8.25%

The shipped implementation returns **9825 cents**.

The correct result is **9743 cents**:

10000 -> 9000 after the 10% order discount
9000 -> 743 tax after 8.25% half-up rounding
9000 + 743 = 9743

The failing test is `PricingTests.TaxIsChargedAfterDiscount`.

Fix the calculator so that the order discount is applied before tax.
Keep money in integer cents.

## Feature to add

Add support for a `GOLD` membership tier.

A GOLD member gets **10% off every LABOR line** before the order-level
discount is applied.

- GOLD affects LABOR lines only.
- PARTS lines are not affected.
- The order-level discount is then applied.
- Tax is charged last.

The required order is:

    Gold labor discount
          ->
    Order discount
          ->
    Tax

Percentage calculations should use the existing rounding policy.

`MembershipTier.GOLD` is already defined, but the calculator currently
ignores membership.

## Input / Output

Input:

- line items (amount in cents, type LABOR/PARTS, quantity)
- order discount percentage
- tax percentage
- optional membership tier

Output:

- total in integer cents

## Run tests

    mvn test
