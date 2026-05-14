package com.upsjb.ms3.shared.pagination;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaginationMapper {

    public <T, R> PagedResponse<R> toPagedResponse(Page<T> page, Function<T, R> mapper) {
        if (page == null) {
            throw new IllegalArgumentException("La página no puede ser nula.");
        }

        if (mapper == null) {
            throw new IllegalArgumentException("El mapper de paginación no puede ser nulo.");
        }

        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();

        return new PagedResponse<>(
                content,
                new PageMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious(),
                        page.getNumberOfElements()
                )
        );
    }

    public record PagedResponse<T>(
            List<T> content,
            PageMetadata page
    ) {
    }

    public record PageMetadata(
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious,
            int numberOfElements
    ) {
    }
}