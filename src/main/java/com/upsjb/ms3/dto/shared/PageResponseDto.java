// ruta: src/main/java/com/upsjb/ms3/dto/shared/PageResponseDto.java
package com.upsjb.ms3.dto.shared;

import java.util.List;
import lombok.Builder;

@Builder
public record PageResponseDto<T>(
        List<T> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext,
        Boolean hasPrevious,
        String sortBy,
        String sortDirection
) {
}