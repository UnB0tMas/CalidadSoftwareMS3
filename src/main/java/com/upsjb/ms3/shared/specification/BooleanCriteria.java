package com.upsjb.ms3.shared.specification;

public record BooleanCriteria(
        Boolean value
) {

    public boolean specified() {
        return value != null;
    }

    public boolean isTrue() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalse() {
        return Boolean.FALSE.equals(value);
    }

    public static BooleanCriteria of(Boolean value) {
        return new BooleanCriteria(value);
    }
}