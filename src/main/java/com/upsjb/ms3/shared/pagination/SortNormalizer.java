package com.upsjb.ms3.shared.pagination;

import com.upsjb.ms3.shared.constants.Ms3Constants;
import com.upsjb.ms3.shared.exception.ValidationException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SortNormalizer {

    private final SortFieldValidator sortFieldValidator;

    public Sort normalize(
            String sortBy,
            String direction,
            Collection<String> allowedFields,
            String defaultField
    ) {
        return normalize(sortBy, direction, allowedFields, defaultField, Map.of());
    }

    public Sort normalize(
            String sortBy,
            String direction,
            Collection<String> allowedFields,
            String defaultField,
            Map<String, String> aliases
    ) {
        String validatedField = sortFieldValidator.validateOrDefault(sortBy, allowedFields, defaultField);
        String resolvedField = resolveAlias(validatedField, aliases);
        Sort.Direction resolvedDirection = resolveDirection(direction);

        return Sort.by(resolvedDirection, resolvedField);
    }

    public Sort.Direction resolveDirection(String direction) {
        if (!StringUtils.hasText(direction)) {
            return Sort.Direction.fromString(Ms3Constants.DEFAULT_SORT_DIRECTION);
        }

        String normalized = direction.trim().toUpperCase(Locale.ROOT);

        if (!normalized.equals("ASC") && !normalized.equals("DESC")) {
            throw new ValidationException(
                    "SORT_DIRECTION_INVALID",
                    "La dirección de ordenamiento debe ser ASC o DESC."
            );
        }

        return Sort.Direction.fromString(normalized);
    }

    private String resolveAlias(String field, Map<String, String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return field;
        }

        return aliases.getOrDefault(field, field);
    }
}