package com.upsjb.ms3.shared.pagination;

import com.upsjb.ms3.shared.exception.ValidationException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SortFieldValidator {

    public String validateOrDefault(
            String requestedField,
            Collection<String> allowedFields,
            String defaultField
    ) {
        String fallback = StringUtils.hasText(defaultField) ? defaultField.trim() : "id";

        if (!StringUtils.hasText(requestedField)) {
            return fallback;
        }

        String normalized = requestedField.trim();

        if (allowedFields == null || allowedFields.isEmpty()) {
            return normalized;
        }

        Set<String> normalizedAllowed = allowedFields.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (!normalizedAllowed.contains(normalized.toLowerCase(Locale.ROOT))) {
            throw new ValidationException(
                    "SORT_FIELD_NOT_ALLOWED",
                    "El campo de ordenamiento no está permitido: " + requestedField + "."
            );
        }

        return normalized;
    }
}