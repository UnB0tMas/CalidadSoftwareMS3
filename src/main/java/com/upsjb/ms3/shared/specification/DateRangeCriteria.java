package com.upsjb.ms3.shared.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DateRangeCriteria<T extends Comparable<? super T>>(
        T from,
        T to
) {

    public boolean hasFrom() {
        return from != null;
    }

    public boolean hasTo() {
        return to != null;
    }

    public boolean hasAny() {
        return hasFrom() || hasTo();
    }

    public static DateRangeCriteria<LocalDate> of(LocalDate from, LocalDate to) {
        return new DateRangeCriteria<>(from, to);
    }

    public static DateRangeCriteria<LocalDateTime> of(LocalDateTime from, LocalDateTime to) {
        return new DateRangeCriteria<>(from, to);
    }
}