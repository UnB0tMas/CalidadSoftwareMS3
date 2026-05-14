package com.upsjb.ms3.shared.specification;

import org.springframework.data.jpa.domain.Specification;

public final class SpecificationBuilder<T> {

    private Specification<T> specification;

    private SpecificationBuilder() {
        this.specification = SpecificationUtils.alwaysTrue();
    }

    public static <T> SpecificationBuilder<T> create() {
        return new SpecificationBuilder<>();
    }

    public SpecificationBuilder<T> activeOnly() {
        return and(SpecificationUtils.activeOnly());
    }

    public SpecificationBuilder<T> equal(String field, Object value) {
        return and(SpecificationUtils.equal(field, value));
    }

    public SpecificationBuilder<T> like(String field, String value) {
        return and(SpecificationUtils.likeIgnoreCase(field, value));
    }

    public SpecificationBuilder<T> textSearch(String value, String... fields) {
        return and(SpecificationUtils.orLikeIgnoreCase(value, fields));
    }

    public <C extends Comparable<? super C>> SpecificationBuilder<T> range(
            String field,
            DateRangeCriteria<C> criteria
    ) {
        return and(SpecificationUtils.between(field, criteria));
    }

    public <N extends Number & Comparable<? super N>> SpecificationBuilder<T> numericRange(
            String field,
            NumericRangeCriteria<N> criteria
    ) {
        return and(SpecificationUtils.numericBetween(field, criteria));
    }

    public SpecificationBuilder<T> bool(String field, BooleanCriteria criteria) {
        return and(SpecificationUtils.booleanEquals(field, criteria));
    }

    public SpecificationBuilder<T> and(Specification<T> other) {
        if (other != null) {
            specification = specification.and(other);
        }

        return this;
    }

    public SpecificationBuilder<T> or(Specification<T> other) {
        if (other != null) {
            specification = specification.or(other);
        }

        return this;
    }

    public Specification<T> build() {
        return specification;
    }
}