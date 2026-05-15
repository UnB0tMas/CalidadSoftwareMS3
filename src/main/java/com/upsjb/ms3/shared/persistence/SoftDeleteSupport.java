package com.upsjb.ms3.shared.persistence;

import com.upsjb.ms3.domain.entity.AuditableEntity;
import com.upsjb.ms3.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SoftDeleteSupport {

    public <T extends AuditableEntity> T activate(T entity, String entityName) {
        ensureEntity(entity, entityName);
        entity.activar();
        return entity;
    }

    public <T extends AuditableEntity> T deactivate(T entity, String entityName) {
        ensureEntity(entity, entityName);
        entity.inactivar();
        return entity;
    }

    public boolean isActive(AuditableEntity entity) {
        return entity != null && entity.isActivo();
    }

    public boolean isInactive(AuditableEntity entity) {
        return entity != null && !entity.isActivo();
    }

    private void ensureEntity(AuditableEntity entity, String entityName) {
        if (entity == null) {
            throw new NotFoundException(
                    "RESOURCE_NOT_FOUND",
                    safeEntityName(entityName) + " no encontrado."
            );
        }
    }

    private String safeEntityName(String entityName) {
        return entityName == null || entityName.isBlank() ? "Registro" : entityName.trim();
    }
}