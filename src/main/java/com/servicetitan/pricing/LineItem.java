package com.servicetitan.pricing;

import java.util.Objects;

public final class LineItem {
    private final long amountInCents;
    private final LineItemType type;
    private final int quantity;

    public LineItem(long amountInCents, LineItemType type, int quantity) {
        if (amountInCents < 0) {
            throw new IllegalArgumentException("amountInCents cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.amountInCents = amountInCents;
        this.type = Objects.requireNonNull(type);
        this.quantity = quantity;
    }

    public long getAmountInCents() {
        return amountInCents;
    }

    public LineItemType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public long totalInCents() {
        return amountInCents * quantity;
    }
}
