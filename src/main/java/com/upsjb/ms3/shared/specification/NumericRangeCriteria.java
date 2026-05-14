package com.upsjb.ms3.shared.specification;

public record NumericRangeCriteria<N extends Number & Comparable<? super N>>(
        N min,
        N max
) {

    public boolean hasMin() {
        return min != null;
    }

    public boolean hasMax() {
        return max != null;
    }

    public boolean hasAny() {
        return hasMin() || hasMax();
    }
}