// ruta: src/main/java/com/upsjb/ms3/specification/SpecificationFilterSupport.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

final class SpecificationFilterSupport {

    private SpecificationFilterSupport() {
    }

    static DateRangeCriteria<LocalDateTime> dateRange(DateRangeFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return DateRangeCriteria.of(filter.fechaInicio(), filter.fechaFin());
    }

    static LocalDateTime dateTime(Object source, String... methodNames) {
        return value(source, LocalDateTime.class, methodNames);
    }

    static String text(Object source, String... methodNames) {
        return value(source, String.class, methodNames);
    }

    static Boolean bool(Object source, String... methodNames) {
        return value(source, Boolean.class, methodNames);
    }

    static Integer integer(Object source, String... methodNames) {
        return value(source, Integer.class, methodNames);
    }

    static Long longValue(Object source, String... methodNames) {
        return value(source, Long.class, methodNames);
    }

    static <T> T value(Object source, Class<T> type, String... methodNames) {
        if (source == null || type == null || methodNames == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = source.getClass().getMethod(methodName);
                Object value = method.invoke(source);

                if (type.isInstance(value)) {
                    return type.cast(value);
                }
            } catch (ReflectiveOperationException ignored) {
                // Permite compatibilidad defensiva entre DTOs con nombres distintos.
            }
        }

        return null;
    }
}