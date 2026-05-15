package com.upsjb.ms3.shared.persistence;

import com.upsjb.ms3.domain.entity.AuditableEntity;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class EntityStateValidator {

    public <T extends AuditableEntity> T requireExists(T entity, String entityName) {
        if (entity == null) {
            throw new NotFoundException(
                    "RESOURCE_NOT_FOUND",
                    safeEntityName(entityName) + " no encontrado."
            );
        }

        return entity;
    }

    public <T extends AuditableEntity> T requireActive(T entity, String entityName) {
        requireExists(entity, entityName);

        if (!entity.isActivo()) {
            throw new NotFoundException(
                    "ACTIVE_RESOURCE_NOT_FOUND",
                    safeEntityName(entityName) + " activo no encontrado."
            );
        }

        return entity;
    }

    public <T extends AuditableEntity> T requireInactive(T entity, String entityName) {
        requireExists(entity, entityName);

        if (entity.isActivo()) {
            throw new ConflictException(
                    "RESOURCE_ALREADY_ACTIVE",
                    safeEntityName(entityName) + " ya se encuentra activo."
            );
        }

        return entity;
    }

    public <T extends AuditableEntity> T requireCanDeactivate(T entity, String entityName) {
        requireActive(entity, entityName);
        return entity;
    }

    public <T extends AuditableEntity> T requireCanActivate(T entity, String entityName) {
        requireExists(entity, entityName);

        if (entity.isActivo()) {
            throw new ConflictException(
                    "RESOURCE_ALREADY_ACTIVE",
                    safeEntityName(entityName) + " ya se encuentra activo."
            );
        }

        return entity;
    }

    public void requireTrue(boolean condition, String code, String message) {
        if (!condition) {
            throw new ConflictException(code, message);
        }
    }

    public void requireFalse(boolean condition, String code, String message) {
        if (condition) {
            throw new ConflictException(code, message);
        }
    }

    private String safeEntityName(String entityName) {
        return entityName == null || entityName.isBlank() ? "Registro" : entityName.trim();
    }
}