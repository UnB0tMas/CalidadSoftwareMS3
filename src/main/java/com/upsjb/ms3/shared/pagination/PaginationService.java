// ruta: src/main/java/com/upsjb/ms3/shared/pagination/PaginationService.java
package com.upsjb.ms3.shared.pagination;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.shared.constants.Ms3Constants;
import com.upsjb.ms3.shared.exception.ValidationException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaginationService {

    private final AppPropertiesConfig appProperties;
    private final SortNormalizer sortNormalizer;
    private final PaginationMapper paginationMapper;

    public Pageable pageable(
            Integer page,
            Integer size,
            String sortBy,
            String direction,
            Collection<String> allowedSortFields,
            String defaultSortField
    ) {
        return pageable(page, size, sortBy, direction, allowedSortFields, defaultSortField, Map.of());
    }

    public Pageable pageable(
            Integer page,
            Integer size,
            String sortBy,
            String direction,
            Collection<String> allowedSortFields,
            String defaultSortField,
            Map<String, String> sortAliases
    ) {
        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);

        return PageRequest.of(
                resolvedPage,
                resolvedSize,
                sortNormalizer.normalize(sortBy, direction, allowedSortFields, defaultSortField, sortAliases)
        );
    }

    public Pageable defaultPageable(String defaultSortField) {
        return pageable(
                Ms3Constants.DEFAULT_PAGE,
                appProperties.getPagination().getDefaultSize(),
                defaultSortField,
                appProperties.getPagination().getDefaultSortDirection(),
                null,
                defaultSortField
        );
    }

    public <T, R> PaginationMapper.PagedResponse<R> toResponse(Page<T> page, Function<T, R> mapper) {
        return paginationMapper.toPagedResponse(page, mapper);
    }

    public <T, R> PageResponseDto<R> toPageResponseDto(Page<T> page, Function<T, R> mapper) {
        PaginationMapper.PagedResponse<R> paged = toResponse(page, mapper);

        Sort.Order order = page.getSort()
                .stream()
                .findFirst()
                .orElse(null);

        return PageResponseDto.<R>builder()
                .content(paged.content())
                .page(paged.page().number())
                .size(paged.page().size())
                .totalElements(paged.page().totalElements())
                .totalPages(paged.page().totalPages())
                .hasNext(paged.page().hasNext())
                .hasPrevious(paged.page().hasPrevious())
                .sortBy(order == null ? null : order.getProperty())
                .sortDirection(order == null ? null : order.getDirection().name())
                .build();
    }

    private int resolvePage(Integer page) {
        if (page == null) {
            return Ms3Constants.DEFAULT_PAGE;
        }

        if (page < 0) {
            throw new ValidationException(
                    "PAGE_NUMBER_INVALID",
                    "El número de página no puede ser negativo."
            );
        }

        return page;
    }

    private int resolveSize(Integer size) {
        int defaultSize = appProperties.getPagination().getDefaultSize() == null
                ? Ms3Constants.DEFAULT_PAGE_SIZE
                : appProperties.getPagination().getDefaultSize();

        int maxSize = appProperties.getPagination().getMaxSize() == null
                ? Ms3Constants.MAX_PAGE_SIZE
                : appProperties.getPagination().getMaxSize();

        if (size == null) {
            return defaultSize;
        }

        if (size < 1) {
            throw new ValidationException(
                    "PAGE_SIZE_INVALID",
                    "El tamaño de página debe ser mayor a cero."
            );
        }

        if (size > maxSize) {
            throw new ValidationException(
                    "PAGE_SIZE_TOO_LARGE",
                    "El tamaño de página no puede superar " + maxSize + " registros."
            );
        }

        return size;
    }
}