package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.AuditableEntity;
import com.upsjb.ms3.shared.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class EntityDisplayResolver {

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;

    @PersistenceContext
    private EntityManager entityManager;

    public String display(Object entity) {
        if (entity == null) {
            return null;
        }

        return firstText(entity,
                "nombre",
                "razonSocial",
                "nombreComercial",
                "nombresCompletos",
                "codigoProducto",
                "codigoSku",
                "codigoEmpleado",
                "codigo",
                "slug",
                "barcode"
        ).orElseGet(() -> String.valueOf(resolveId(entity).orElse("")));
    }

    public String value(Object entity) {
        if (entity == null) {
            return null;
        }

        return firstText(entity,
                "codigo",
                "codigoProducto",
                "codigoSku",
                "codigoEmpleado",
                "slug",
                "barcode",
                "ruc",
                "numeroDocumento",
                "nombre"
        ).orElseGet(() -> String.valueOf(resolveId(entity).orElse("")));
    }

    public Optional<Object> resolveId(Object entity) {
        if (entity == null) {
            return Optional.empty();
        }

        for (String fieldName : List.of(
                "idTipoProducto",
                "idCategoria",
                "idMarca",
                "idAtributo",
                "idProducto",
                "idSku",
                "idProveedor",
                "idAlmacen",
                "idPromocion",
                "idEmpleadoSnapshot"
        )) {
            Optional<Object> value = read(entity, fieldName);

            if (value.isPresent()) {
                return value;
            }
        }

        return Optional.empty();
    }

    public boolean isActive(Object entity) {
        if (entity instanceof AuditableEntity auditableEntity) {
            return auditableEntity.isActivo();
        }

        return read(entity, "estado")
                .map(value -> {
                    if (value instanceof Boolean booleanValue) {
                        return booleanValue;
                    }
                    return Boolean.parseBoolean(String.valueOf(value));
                })
                .orElse(true);
    }

    public Map<String, Object> metadata(Object entity, String... fields) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        if (entity == null || fields == null) {
            return metadata;
        }

        for (String field : fields) {
            read(entity, field).ifPresent(value -> metadata.put(field, value));
        }

        return metadata;
    }

    @Transactional(readOnly = true)
    public <T> T resolveRequired(
            Class<T> entityClass,
            String entityName,
            String idField,
            Object id,
            Map<String, Object> references
    ) {
        return resolveOptional(entityClass, idField, id, references)
                .orElseThrow(() -> NotFoundException.activeResource(entityName, resolveReferenceLabel(id, references)));
    }

    @Transactional(readOnly = true)
    public <T> Optional<T> resolveOptional(
            Class<T> entityClass,
            String idField,
            Object id,
            Map<String, Object> references
    ) {
        if (id != null) {
            Optional<T> byId = findByField(entityClass, idField, id, true);

            if (byId.isPresent()) {
                return byId;
            }
        }

        if (references == null || references.isEmpty()) {
            return Optional.empty();
        }

        for (Map.Entry<String, Object> entry : references.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            Optional<T> result = findByField(entityClass, entry.getKey(), entry.getValue(), true);

            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public <T> List<T> searchActive(
            Class<T> entityClass,
            List<String> fields,
            String search,
            Integer limit
    ) {
        if (!StringUtils.hasText(search) || fields == null || fields.isEmpty()) {
            return List.of();
        }

        int resolvedLimit = resolveLimit(limit);

        List<String> predicates = new ArrayList<>();
        for (String field : fields) {
            predicates.add("LOWER(CAST(e." + field + " AS string)) LIKE :search");
        }

        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e "
                + "WHERE e.estado = true AND ("
                + String.join(" OR ", predicates)
                + ")";

        TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
        query.setParameter("search", "%" + search.trim().toLowerCase() + "%");
        query.setMaxResults(resolvedLimit);

        return query.getResultList();
    }

    private <T> Optional<T> findByField(
            Class<T> entityClass,
            String fieldName,
            Object value,
            boolean activeOnly
    ) {
        if (!StringUtils.hasText(fieldName) || value == null) {
            return Optional.empty();
        }

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT e FROM ")
                .append(entityClass.getSimpleName())
                .append(" e WHERE ");

        if (value instanceof String) {
            jpql.append("LOWER(CAST(e.")
                    .append(fieldName)
                    .append(" AS string)) = LOWER(:value)");
        } else {
            jpql.append("e.")
                    .append(fieldName)
                    .append(" = :value");
        }

        if (activeOnly) {
            jpql.append(" AND e.estado = true");
        }

        TypedQuery<T> query = entityManager.createQuery(jpql.toString(), entityClass);
        query.setParameter("value", value instanceof String text ? text.trim() : value);
        query.setMaxResults(1);

        return query.getResultStream().findFirst();
    }

    private Optional<String> firstText(Object entity, String... fields) {
        for (String field : fields) {
            Optional<Object> value = read(entity, field);

            if (value.isPresent() && StringUtils.hasText(String.valueOf(value.get()))) {
                return Optional.of(String.valueOf(value.get()).trim());
            }
        }

        return Optional.empty();
    }

    private Optional<Object> read(Object entity, String fieldName) {
        if (entity == null || !StringUtils.hasText(fieldName)) {
            return Optional.empty();
        }

        Class<?> current = entity.getClass();

        while (current != null && !Object.class.equals(current)) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return Optional.ofNullable(field.get(entity));
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (IllegalAccessException ex) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Object resolveReferenceLabel(Object id, Map<String, Object> references) {
        if (id != null) {
            return id;
        }

        if (references == null || references.isEmpty()) {
            return "sin referencia";
        }

        return references;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(limit, MAX_LOOKUP_LIMIT);
    }
}