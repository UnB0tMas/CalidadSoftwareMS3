package com.upsjb.ms3.shared.persistence;

import com.upsjb.ms3.domain.entity.AuditableEntity;
import com.upsjb.ms3.shared.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveRecordResolver {

    private final EntityStateValidator entityStateValidator;

    public <T extends AuditableEntity, ID> T findById(
            JpaRepository<T, ID> repository,
            ID id,
            String entityName
    ) {
        validateRepository(repository);
        validateId(id, entityName);

        T entity = repository.findById(id)
                .orElseThrow(() -> NotFoundException.resource(safeEntityName(entityName), id));

        return entityStateValidator.requireExists(entity, entityName);
    }

    public <T extends AuditableEntity, ID> T findActiveById(
            JpaRepository<T, ID> repository,
            ID id,
            String entityName
    ) {
        T entity = findById(repository, id, entityName);
        return entityStateValidator.requireActive(entity, entityName);
    }

    public <T extends AuditableEntity> T fromOptional(
            Optional<T> optional,
            String entityName,
            Object reference
    ) {
        T entity = optional.orElseThrow(() -> NotFoundException.resource(safeEntityName(entityName), reference));
        return entityStateValidator.requireExists(entity, entityName);
    }

    public <T extends AuditableEntity> T activeFromOptional(
            Optional<T> optional,
            String entityName,
            Object reference
    ) {
        T entity = fromOptional(optional, entityName, reference);
        return entityStateValidator.requireActive(entity, entityName);
    }

    private void validateRepository(Object repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repository no puede ser nulo.");
        }
    }

    private void validateId(Object id, String entityName) {
        if (id == null) {
            throw new NotFoundException(
                    "RESOURCE_ID_REQUIRED",
                    "El identificador de " + safeEntityName(entityName) + " es obligatorio."
            );
        }
    }

    private String safeEntityName(String entityName) {
        return entityName == null || entityName.isBlank() ? "Registro" : entityName.trim();
    }
}