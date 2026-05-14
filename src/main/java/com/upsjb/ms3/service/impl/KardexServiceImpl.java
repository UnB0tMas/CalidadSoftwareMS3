// ruta: src/main/java/com/upsjb/ms3/service/impl/KardexServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.filter.KardexFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.KardexResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.KardexMapper;
import com.upsjb.ms3.policy.KardexPolicy;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.KardexService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.KardexSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.KardexValidator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements KardexService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idMovimiento",
            "codigoMovimiento",
            "sku.codigoSku",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "almacen.codigo",
            "almacen.nombre",
            "tipoMovimiento",
            "motivoMovimiento",
            "cantidad",
            "stockAnterior",
            "stockNuevo",
            "referenciaTipo",
            "referenciaIdExterno",
            "actorIdUsuarioMs1",
            "actorIdEmpleadoMs2",
            "estadoMovimiento",
            "requestId",
            "correlationId",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final KardexMapper kardexMapper;
    private final KardexValidator kardexValidator;
    private final KardexPolicy kardexPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<KardexResponseDto>> consultar(
            KardexFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        boolean canViewCosts = authorizeAndResolveCostVisibility(actor, incluirCostos);

        kardexValidator.validateFilter(filter);

        PageRequestDto safePage = safePageRequest(pageRequest);
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<KardexResponseDto> response = paginationService.toPageResponseDto(
                movimientoInventarioRepository.findAll(KardexSpecifications.fromFilter(filter), pageable),
                movimiento -> kardexMapper.toResponse(movimiento, canViewCosts)
        );

        log.info(
                "Kardex consultado. actor={}, idSku={}, idAlmacen={}, referenciaTipo={}, referenciaIdExterno={}, total={}",
                actor.actorLabel(),
                filter.idSku(),
                filter.idAlmacen(),
                filter.referenciaTipo(),
                filter.referenciaIdExterno(),
                response.totalElements()
        );

        return apiResponseFactory.dtoOk(
                "Kardex obtenido correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<KardexResponseDto> obtenerMovimiento(
            Long idMovimiento,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        boolean canViewCosts = authorizeAndResolveCostVisibility(actor, incluirCostos);

        MovimientoInventario movimiento = findMovimientoRequired(idMovimiento);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                kardexMapper.toResponse(movimiento, canViewCosts)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorSku(
            Long idSku,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        if (idSku == null) {
            throw new ValidationException(
                    "KARDEX_SKU_REQUERIDO",
                    "Debe indicar el SKU para consultar kardex."
            );
        }

        KardexFilterDto filter = KardexFilterDto.builder()
                .idSku(idSku)
                .estado(Boolean.TRUE)
                .build();

        return consultar(filter, pageRequest, incluirCostos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorAlmacen(
            Long idAlmacen,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        if (idAlmacen == null) {
            throw new ValidationException(
                    "KARDEX_ALMACEN_REQUERIDO",
                    "Debe indicar el almacén para consultar kardex."
            );
        }

        KardexFilterDto filter = KardexFilterDto.builder()
                .idAlmacen(idAlmacen)
                .estado(Boolean.TRUE)
                .build();

        return consultar(filter, pageRequest, incluirCostos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorReferencia(
            String referenciaTipo,
            String referenciaIdExterno,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        String cleanTipo = StringNormalizer.cleanOrNull(referenciaTipo);
        String cleanReferencia = StringNormalizer.cleanOrNull(referenciaIdExterno);

        kardexValidator.validateReferenceSearch(cleanTipo, cleanReferencia);

        KardexFilterDto filter = KardexFilterDto.builder()
                .referenciaTipo(cleanTipo)
                .referenciaIdExterno(cleanReferencia)
                .estado(Boolean.TRUE)
                .build();

        return consultar(filter, pageRequest, incluirCostos);
    }

    private boolean authorizeAndResolveCostVisibility(
            AuthenticatedUserContext actor,
            Boolean incluirCostos
    ) {
        boolean employeeCanViewKardex = actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeConsultarKardex(actor.getIdUsuarioMs1());

        kardexPolicy.ensureCanViewKardex(actor, employeeCanViewKardex);

        if (Boolean.TRUE.equals(incluirCostos)) {
            kardexPolicy.ensureCanViewCosts(actor);
            kardexValidator.validateCanViewCost(kardexPolicy.canViewCosts(actor));
            return true;
        }

        return kardexPolicy.canViewCosts(actor);
    }

    private MovimientoInventario findMovimientoRequired(Long idMovimiento) {
        if (idMovimiento == null) {
            throw new ValidationException(
                    "KARDEX_MOVIMIENTO_REQUERIDO",
                    "Debe indicar el movimiento solicitado."
            );
        }

        return movimientoInventarioRepository.findByIdMovimientoAndEstadoTrue(idMovimiento)
                .orElseThrow(() -> new NotFoundException(
                        "MOVIMIENTO_INVENTARIO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy("createdAt")
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }
}