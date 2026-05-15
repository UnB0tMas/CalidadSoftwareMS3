package com.upsjb.ms3.shared.persistence;

import com.upsjb.ms3.domain.entity.AuditableEntity;
import com.upsjb.ms3.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EntityLookupService {

    private final EntityStateValidator entityStateValidator;

    public <T> T resolveRequired(
            String entityName,
            Object reference,
            Supplier<Optional<T>> lookup
    ) {
        validateLookup(lookup);

        return lookup.get()
                .orElseThrow(() -> NotFoundException.resource(safeEntityName(entityName), reference));
    }

    public <T extends AuditableEntity> T resolveActiveRequired(
            String entityName,
            Object reference,
            Supplier<Optional<T>> lookup
    ) {
        T entity = resolveRequired(entityName, reference, lookup);
        return entityStateValidator.requireActive(entity, entityName);
    }

    public <T> Optional<T> resolveOptional(Supplier<Optional<T>> lookup) {
        validateLookup(lookup);
        return lookup.get();
    }

    public <T extends AuditableEntity> Optional<T> resolveActiveOptional(Supplier<Optional<T>> lookup) {
        validateLookup(lookup);

        return lookup.get()
                .filter(AuditableEntity::isActivo);
    }

    public void requireReference(Object reference, String message) {
        if (reference == null) {
            throw new NotFoundException(
                    "REFERENCE_REQUIRED",
                    StringUtils.hasText(message) ? message : "La referencia es obligatoria."
            );
        }

        if (reference instanceof String text && !StringUtils.hasText(text)) {
            throw new NotFoundException(
                    "REFERENCE_REQUIRED",
                    StringUtils.hasText(message) ? message : "La referencia es obligatoria."
            );
        }
    }

    private void validateLookup(Supplier<?> lookup) {
        if (lookup == null) {
            throw new IllegalArgumentException("El lookup no puede ser nulo.");
        }
    }

    private String safeEntityName(String entityName) {
        return entityName == null || entityName.isBlank() ? "Registro" : entityName.trim();
    }
}