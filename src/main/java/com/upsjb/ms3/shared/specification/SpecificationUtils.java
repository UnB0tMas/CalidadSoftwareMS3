package com.upsjb.ms3.shared.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static <T> Specification<T> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static <T> Specification<T> activeOnly() {
        return equal("estado", Boolean.TRUE);
    }

    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.equal(resolvePath(root, field), value);
    }

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }

            Expression<String> expression = cb.lower(resolvePath(root, field).as(String.class));
            return cb.like(expression, "%" + value.trim().toLowerCase(Locale.ROOT) + "%");
        };
    }

    public static <T, C extends Comparable<? super C>> Specification<T> between(
            String field,
            DateRangeCriteria<C> criteria
    ) {
        return comparableBetween(field, criteria == null ? null : criteria.from(), criteria == null ? null : criteria.to());
    }

    public static <T, N extends Number & Comparable<? super N>> Specification<T> numericBetween(
            String field,
            NumericRangeCriteria<N> criteria
    ) {
        return comparableBetween(field, criteria == null ? null : criteria.min(), criteria == null ? null : criteria.max());
    }

    public static <T, C extends Comparable<? super C>> Specification<T> comparableBetween(
            String field,
            C from,
            C to
    ) {
        return (root, query, cb) -> {
            Path<C> path = resolvePath(root, field);

            if (from != null && to != null) {
                return cb.between(path, from, to);
            }

            if (from != null) {
                return cb.greaterThanOrEqualTo(path, from);
            }

            if (to != null) {
                return cb.lessThanOrEqualTo(path, to);
            }

            return cb.conjunction();
        };
    }

    public static <T> Specification<T> booleanEquals(String field, BooleanCriteria criteria) {
        return (root, query, cb) -> criteria == null || !criteria.specified()
                ? cb.conjunction()
                : cb.equal(resolvePath(root, field), criteria.value());
    }

    public static <T> Specification<T> orLikeIgnoreCase(String value, String... fields) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value) || fields == null || fields.length == 0) {
                return cb.conjunction();
            }

            String normalized = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";

            return cb.or(
                    java.util.Arrays.stream(fields)
                            .map(field -> cb.like(cb.lower(resolvePath(root, field).as(String.class)), normalized))
                            .toArray(jakarta.persistence.criteria.Predicate[]::new)
            );
        };
    }

    @SuppressWarnings("unchecked")
    public static <X> Path<X> resolvePath(Path<?> root, String field) {
        if (!StringUtils.hasText(field)) {
            throw new IllegalArgumentException("El campo de specification es obligatorio.");
        }

        Path<?> path = root;

        for (String part : field.split("\\.")) {
            path = path.get(part);
        }

        return (Path<X>) path;
    }
}