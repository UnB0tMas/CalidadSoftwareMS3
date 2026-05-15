// ruta: src/main/java/com/upsjb/ms3/service/impl/SkuAtributoValorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.filter.SkuAtributoValorFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.SkuAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.SkuAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.ProductoSkuSnapshotPayload;
import com.upsjb.ms3.mapper.SkuAtributoValorMapper;
import com.upsjb.ms3.policy.ProductoSkuPolicy;
import com.upsjb.ms3.repository.AtributoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.SkuAtributoValorService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.SkuAtributoValorSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.AtributoValidator;
import com.upsjb.ms3.validator.ProductoSkuValidator;
import com.upsjb.ms3.validator.SkuAtributoValorValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkuAtributoValorServiceImpl implements SkuAtributoValorService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idSkuAtributoValor",
            "sku.codigoSku",
            "sku.barcode",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "atributo.codigo",
            "atributo.nombre",
            "atributo.tipoDato",
            "valorTexto",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_CODIGO_SKU_LENGTH = 100;
    private static final int MAX_BARCODE_LENGTH = 100;
    private static final int MAX_CODIGO_PRODUCTO_LENGTH = 80;
    private static final int MAX_NOMBRE_PRODUCTO_LENGTH = 180;
    private static final int MAX_CODIGO_ATRIBUTO_LENGTH = 50;
    private static final int MAX_NOMBRE_ATRIBUTO_LENGTH = 120;
    private static final int MAX_VALOR_TEXTO_LENGTH = 500;
    private static final int MAX_MOTIVO_LENGTH = 500;

    private final ProductoSkuRepository productoSkuRepository;
    private final AtributoRepository atributoRepository;
    private final SkuAtributoValorRepository skuAtributoValorRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final SkuAtributoValorMapper skuAtributoValorMapper;
    private final ProductoSkuValidator productoSkuValidator;
    private final AtributoValidator atributoValidator;
    private final SkuAtributoValorValidator skuAtributoValorValidator;
    private final ProductoSkuPolicy productoSkuPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<SkuAtributoValorResponseDto> guardarValor(
            Long idSku,
            SkuAtributoValorRequestDto request
    ) {
        return guardarValor(EntityReferenceDto.builder().id(idSku).build(), request);
    }

    @Override
    @Transactional
    public ApiResponseDto<SkuAtributoValorResponseDto> guardarValor(
            EntityReferenceDto skuReference,
            SkuAtributoValorRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        ProductoSku sku = resolveSku(skuReference);
        productoSkuValidator.requireActive(sku);

        SkuAtributoValorRequestDto normalized = normalizeRequest(request);
        Atributo atributo = resolveAtributo(normalized.atributo());
        TipoProductoAtributo relation = resolveRelationRequired(sku, atributo);

        validateValue(sku, atributo, relation, normalized);

        SkuAtributoValor entity = skuAtributoValorRepository
                .findBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrue(sku.getIdSku(), atributo.getIdAtributo())
                .orElse(null);

        boolean created = entity == null;
        Map<String, Object> before = created ? null : auditSnapshot(entity);

        if (created) {
            entity = skuAtributoValorMapper.toEntity(normalized, sku, atributo);
            entity.activar();
        } else {
            skuAtributoValorMapper.updateEntity(entity, normalized, atributo);
        }

        SkuAtributoValor saved = skuAtributoValorRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_ACTUALIZADO,
                EntidadAuditada.SKU_ATRIBUTO_VALOR,
                String.valueOf(saved.getIdSkuAtributoValor()),
                created ? "CREAR_VALOR_ATRIBUTO_SKU" : "ACTUALIZAR_VALOR_ATRIBUTO_SKU",
                created ? "Atributo de SKU creado correctamente." : "Atributo de SKU actualizado correctamente.",
                auditMetadata(saved, actor, before == null ? null : Map.of("before", before))
        );

        registrarOutboxSkuActualizado(sku);

        log.info(
                "Valor de atributo de SKU guardado. idSku={}, codigoSku={}, idAtributo={}, actor={}",
                sku.getIdSku(),
                sku.getCodigoSku(),
                atributo.getIdAtributo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                skuAtributoValorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<List<SkuAtributoValorResponseDto>> reemplazarValores(
            Long idSku,
            List<SkuAtributoValorRequestDto> request
    ) {
        return reemplazarValores(EntityReferenceDto.builder().id(idSku).build(), request);
    }

    @Override
    @Transactional
    public ApiResponseDto<List<SkuAtributoValorResponseDto>> reemplazarValores(
            EntityReferenceDto skuReference,
            List<SkuAtributoValorRequestDto> request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        ProductoSku sku = resolveSku(skuReference);
        productoSkuValidator.requireActive(sku);

        List<TipoProductoAtributo> plantilla = findPlantillaActiva(sku);
        List<ValorPreparado> preparados = prepararValores(sku, request, plantilla);

        Set<Long> atributosProcesados = new LinkedHashSet<>();
        preparados.forEach(preparado -> atributosProcesados.add(preparado.atributo().getIdAtributo()));
        skuAtributoValorValidator.validateRequiredAttributesPresent(plantilla, atributosProcesados);

        List<SkuAtributoValor> actuales = skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku());

        Map<Long, SkuAtributoValor> actualesPorAtributo = new LinkedHashMap<>();
        for (SkuAtributoValor actual : actuales) {
            if (actual.getAtributo() != null && actual.getAtributo().getIdAtributo() != null) {
                actualesPorAtributo.put(actual.getAtributo().getIdAtributo(), actual);
            }
        }

        List<SkuAtributoValor> toPersist = new ArrayList<>();
        Set<Long> atributosReemplazados = new LinkedHashSet<>();

        for (ValorPreparado preparado : preparados) {
            Long idAtributo = preparado.atributo().getIdAtributo();
            atributosReemplazados.add(idAtributo);

            SkuAtributoValor entity = actualesPorAtributo.get(idAtributo);
            if (entity == null) {
                entity = skuAtributoValorMapper.toEntity(preparado.request(), sku, preparado.atributo());
            } else {
                skuAtributoValorMapper.updateEntity(entity, preparado.request(), preparado.atributo());
            }

            entity.activar();
            toPersist.add(entity);
        }

        for (SkuAtributoValor actual : actuales) {
            Long idAtributo = actual.getAtributo() == null ? null : actual.getAtributo().getIdAtributo();

            if (idAtributo != null && !atributosReemplazados.contains(idAtributo)) {
                actual.inactivar();
                toPersist.add(actual);
            }
        }

        List<SkuAtributoValor> saved = skuAtributoValorRepository.saveAllAndFlush(toPersist);

        List<SkuAtributoValorResponseDto> response = saved.stream()
                .filter(SkuAtributoValor::isActivo)
                .map(skuAtributoValorMapper::toResponse)
                .toList();

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_SKU,
                String.valueOf(sku.getIdSku()),
                "REEMPLAZAR_ATRIBUTOS_SKU",
                "Atributos de SKU reemplazados correctamente.",
                Map.of(
                        "idSku", sku.getIdSku(),
                        "codigoSku", sku.getCodigoSku(),
                        "cantidadAtributos", response.size(),
                        "actor", actor.actorLabel()
                )
        );

        registrarOutboxSkuActualizado(sku);

        log.info(
                "Atributos de SKU reemplazados. idSku={}, codigoSku={}, cantidad={}, actor={}",
                sku.getIdSku(),
                sku.getCodigoSku(),
                response.size(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SkuAtributoValorResponseDto>> listarPorSku(Long idSku) {
        return listarPorSku(EntityReferenceDto.builder().id(idSku).build());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SkuAtributoValorResponseDto>> listarPorSku(EntityReferenceDto skuReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        ProductoSku sku = resolveSku(skuReference);

        List<SkuAtributoValorResponseDto> response = skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku())
                .stream()
                .map(skuAtributoValorMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<SkuAtributoValorResponseDto>> listar(
            SkuAtributoValorFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");
        SkuAtributoValorFilterDto normalizedFilter = normalizeFilter(filter);

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<SkuAtributoValorResponseDto> response = paginationService.toPageResponseDto(
                skuAtributoValorRepository.findAll(
                        SkuAtributoValorSpecifications.fromFilter(normalizedFilter),
                        pageable
                ),
                skuAtributoValorMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<SkuAtributoValorResponseDto> obtenerDetalle(Long idSkuAtributoValor) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        SkuAtributoValor entity = findAnyValorRequired(idSkuAtributoValor);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                skuAtributoValorMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<SkuAtributoValorResponseDto> inactivar(
            Long idSkuAtributoValor,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        String motivo = validateEstadoChangeRequestAndReturnMotivo(request);

        SkuAtributoValor entity = findActiveValorRequired(idSkuAtributoValor);
        TipoProductoAtributo relation = resolveRelationOrNull(entity.getSku(), entity.getAtributo());

        skuAtributoValorValidator.validateCanInactivate(entity, relation);

        Map<String, Object> before = auditSnapshot(entity);
        entity.inactivar();

        SkuAtributoValor saved = skuAtributoValorRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_ACTUALIZADO,
                EntidadAuditada.SKU_ATRIBUTO_VALOR,
                String.valueOf(saved.getIdSkuAtributoValor()),
                "INACTIVAR_VALOR_ATRIBUTO_SKU",
                "Atributo de SKU inactivado correctamente.",
                auditMetadata(saved, actor, Map.of("before", before, "motivo", motivo))
        );

        registrarOutboxSkuActualizado(saved.getSku());

        log.info(
                "Valor de atributo de SKU inactivado. idSkuAtributoValor={}, idSku={}, actor={}",
                saved.getIdSkuAtributoValor(),
                saved.getSku() == null ? null : saved.getSku().getIdSku(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                skuAtributoValorMapper.toResponse(saved)
        );
    }

    private List<ValorPreparado> prepararValores(
            ProductoSku sku,
            List<SkuAtributoValorRequestDto> request,
            List<TipoProductoAtributo> plantilla
    ) {
        List<SkuAtributoValorRequestDto> safeRequest = request == null ? List.of() : request;
        Set<Long> atributosProcesados = new LinkedHashSet<>();
        List<ValorPreparado> preparados = new ArrayList<>();

        for (SkuAtributoValorRequestDto item : safeRequest) {
            SkuAtributoValorRequestDto normalized = normalizeRequest(item);
            Atributo atributo = resolveAtributo(normalized.atributo());
            TipoProductoAtributo relation = findRelationFromTemplate(plantilla, atributo.getIdAtributo());

            skuAtributoValorValidator.validateDuplicateInReplacement(atributo.getIdAtributo(), atributosProcesados);
            validateValue(sku, atributo, relation, normalized);

            preparados.add(new ValorPreparado(normalized, atributo, relation));
        }

        return preparados;
    }

    private void validateValue(
            ProductoSku sku,
            Atributo atributo,
            TipoProductoAtributo relation,
            SkuAtributoValorRequestDto request
    ) {
        skuAtributoValorValidator.validateAssociationAllowed(sku, atributo, relation);

        atributoValidator.validateValueByType(
                atributo,
                request.valorTexto(),
                request.valorNumero(),
                request.valorBoolean(),
                request.valorFecha()
        );

        skuAtributoValorValidator.validateValueByTemplate(
                atributo,
                relation,
                request.valorTexto(),
                request.valorNumero(),
                request.valorBoolean(),
                request.valorFecha()
        );
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU."
            );
        }

        if (reference.id() != null) {
            return productoSkuRepository.findByIdSkuAndEstadoTrue(reference.id())
                    .orElseThrow(this::skuNotFound);
        }

        String codigoSku = firstText(reference.codigoSku(), reference.codigo());
        if (StringNormalizer.hasText(codigoSku)) {
            return productoSkuRepository.findByCodigoSkuIgnoreCaseAndEstadoTrue(codigoSku)
                    .orElseThrow(this::skuNotFound);
        }

        if (StringNormalizer.hasText(reference.barcode())) {
            return productoSkuRepository.findByBarcodeIgnoreCaseAndEstadoTrue(reference.barcode())
                    .orElseThrow(this::skuNotFound);
        }

        throw new ValidationException(
                "SKU_REFERENCIA_INVALIDA",
                "Debe indicar id, codigoSku, código o barcode del SKU."
        );
    }

    private Atributo resolveAtributo(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ATRIBUTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el atributo."
            );
        }

        if (reference.id() != null) {
            return atributoRepository.findByIdAtributoAndEstadoTrue(reference.id())
                    .orElseThrow(this::atributoNotFound);
        }

        if (StringNormalizer.hasText(reference.codigo())) {
            return atributoRepository.findByCodigoIgnoreCaseAndEstadoTrue(reference.codigo())
                    .orElseThrow(this::atributoNotFound);
        }

        if (StringNormalizer.hasText(reference.nombre())) {
            return atributoRepository.findByNombreIgnoreCaseAndEstadoTrue(reference.nombre())
                    .orElseThrow(this::atributoNotFound);
        }

        throw new ValidationException(
                "ATRIBUTO_REFERENCIA_INVALIDA",
                "Debe indicar id, código o nombre del atributo."
        );
    }

    private SkuAtributoValor findActiveValorRequired(Long idSkuAtributoValor) {
        if (idSkuAtributoValor == null) {
            throw new ValidationException(
                    "SKU_ATRIBUTO_VALOR_ID_REQUERIDO",
                    "Debe indicar el valor de atributo de SKU."
            );
        }

        return skuAtributoValorRepository.findByIdSkuAtributoValorAndEstadoTrue(idSkuAtributoValor)
                .orElseThrow(() -> new NotFoundException(
                        "SKU_ATRIBUTO_VALOR_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private SkuAtributoValor findAnyValorRequired(Long idSkuAtributoValor) {
        if (idSkuAtributoValor == null) {
            throw new ValidationException(
                    "SKU_ATRIBUTO_VALOR_ID_REQUERIDO",
                    "Debe indicar el valor de atributo de SKU."
            );
        }

        return skuAtributoValorRepository.findByIdSkuAtributoValor(idSkuAtributoValor)
                .orElseThrow(() -> new NotFoundException(
                        "SKU_ATRIBUTO_VALOR_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private SkuAtributoValorRequestDto normalizeRequest(SkuAtributoValorRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "SKU_ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del atributo de SKU."
            );
        }

        return SkuAtributoValorRequestDto.builder()
                .atributo(request.atributo())
                .valorTexto(StringNormalizer.cleanOrNull(request.valorTexto()))
                .valorNumero(request.valorNumero())
                .valorBoolean(request.valorBoolean())
                .valorFecha(request.valorFecha())
                .build();
    }

    private SkuAtributoValorFilterDto normalizeFilter(SkuAtributoValorFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return SkuAtributoValorFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .idSku(filter.idSku())
                .codigoSku(StringNormalizer.truncateOrNull(filter.codigoSku(), MAX_CODIGO_SKU_LENGTH))
                .barcode(StringNormalizer.truncateOrNull(filter.barcode(), MAX_BARCODE_LENGTH))
                .idProducto(filter.idProducto())
                .codigoProducto(StringNormalizer.truncateOrNull(filter.codigoProducto(), MAX_CODIGO_PRODUCTO_LENGTH))
                .nombreProducto(StringNormalizer.truncateOrNull(filter.nombreProducto(), MAX_NOMBRE_PRODUCTO_LENGTH))
                .idAtributo(filter.idAtributo())
                .codigoAtributo(StringNormalizer.truncateOrNull(filter.codigoAtributo(), MAX_CODIGO_ATRIBUTO_LENGTH))
                .nombreAtributo(StringNormalizer.truncateOrNull(filter.nombreAtributo(), MAX_NOMBRE_ATRIBUTO_LENGTH))
                .tipoDato(filter.tipoDato())
                .valorTexto(StringNormalizer.truncateOrNull(filter.valorTexto(), MAX_VALOR_TEXTO_LENGTH))
                .visiblePublico(filter.visiblePublico())
                .filtrable(filter.filtrable())
                .estado(filter.estado())
                .incluirTodosLosEstados(Boolean.TRUE.equals(filter.incluirTodosLosEstados()))
                .fechaCreacion(filter.fechaCreacion())
                .fechaActualizacion(filter.fechaActualizacion())
                .build();
    }

    private TipoProductoAtributo resolveRelationRequired(ProductoSku sku, Atributo atributo) {
        return resolveRelationOrNull(sku, atributo);
    }

    private TipoProductoAtributo resolveRelationOrNull(ProductoSku sku, Atributo atributo) {
        skuAtributoValorValidator.requireSkuWithProductType(sku);

        if (atributo == null || atributo.getIdAtributo() == null) {
            return null;
        }

        Long idTipoProducto = sku.getProducto().getTipoProducto().getIdTipoProducto();

        return tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        idTipoProducto,
                        atributo.getIdAtributo()
                )
                .orElse(null);
    }

    private List<TipoProductoAtributo> findPlantillaActiva(ProductoSku sku) {
        skuAtributoValorValidator.requireSkuWithProductType(sku);

        return tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
                        sku.getProducto().getTipoProducto().getIdTipoProducto()
                );
    }

    private TipoProductoAtributo findRelationFromTemplate(
            List<TipoProductoAtributo> plantilla,
            Long idAtributo
    ) {
        if (plantilla == null || idAtributo == null) {
            return null;
        }

        return plantilla.stream()
                .filter(item -> item.getAtributo() != null)
                .filter(item -> idAtributo.equals(item.getAtributo().getIdAtributo()))
                .findFirst()
                .orElse(null);
    }

    private void registrarOutboxSkuActualizado(ProductoSku sku) {
        eventoDominioOutboxService.registrarEvento(
                AggregateType.SKU,
                String.valueOf(sku.getIdSku()),
                ProductoEventType.SKU_SNAPSHOT_ACTUALIZADO.getCode(),
                toSkuSnapshotPayload(sku)
        );
    }

    private ProductoSkuSnapshotPayload toSkuSnapshotPayload(ProductoSku sku) {
        Producto producto = sku.getProducto();

        List<ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload> atributos = skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku())
                .stream()
                .map(this::toSkuAtributoSnapshotPayload)
                .toList();

        return ProductoSkuSnapshotPayload.builder()
                .idSku(sku.getIdSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .codigoSku(sku.getCodigoSku())
                .barcode(sku.getBarcode())
                .color(sku.getColor())
                .talla(sku.getTalla())
                .material(sku.getMaterial())
                .modelo(sku.getModelo())
                .stockMinimo(sku.getStockMinimo())
                .stockMaximo(sku.getStockMaximo())
                .pesoGramos(sku.getPesoGramos())
                .altoCm(sku.getAltoCm())
                .anchoCm(sku.getAnchoCm())
                .largoCm(sku.getLargoCm())
                .estadoSku(sku.getEstadoSku() == null ? null : sku.getEstadoSku().getCode())
                .estado(sku.getEstado())
                .createdAt(sku.getCreatedAt())
                .updatedAt(sku.getUpdatedAt())
                .atributos(atributos)
                .build();
    }

    private ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload toSkuAtributoSnapshotPayload(
            SkuAtributoValor valor
    ) {
        Atributo atributo = valor.getAtributo();

        return ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload.builder()
                .idSkuAtributoValor(valor.getIdSkuAtributoValor())
                .idAtributo(atributo == null ? null : atributo.getIdAtributo())
                .codigoAtributo(atributo == null ? null : atributo.getCodigo())
                .nombreAtributo(atributo == null ? null : atributo.getNombre())
                .tipoDato(atributo == null || atributo.getTipoDato() == null ? null : atributo.getTipoDato().getCode())
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
                .requerido(atributo == null ? null : atributo.getRequerido())
                .filtrable(atributo == null ? null : atributo.getFiltrable())
                .visiblePublico(atributo == null ? null : atributo.getVisiblePublico())
                .valorTexto(valor.getValorTexto())
                .valorNumero(valor.getValorNumero())
                .valorBoolean(valor.getValorBoolean())
                .valorFecha(valor.getValorFecha())
                .estado(valor.getEstado())
                .createdAt(valor.getCreatedAt())
                .updatedAt(valor.getUpdatedAt())
                .build();
    }

    private Map<String, Object> auditMetadata(
            SkuAtributoValor valor,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(valor);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(SkuAtributoValor valor) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        ProductoSku sku = valor.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Atributo atributo = valor.getAtributo();

        metadata.put("idSkuAtributoValor", valor.getIdSkuAtributoValor());
        metadata.put("idSku", sku == null ? null : sku.getIdSku());
        metadata.put("codigoSku", sku == null ? null : sku.getCodigoSku());
        metadata.put("idProducto", producto == null ? null : producto.getIdProducto());
        metadata.put("codigoProducto", producto == null ? null : producto.getCodigoProducto());
        metadata.put("idAtributo", atributo == null ? null : atributo.getIdAtributo());
        metadata.put("codigoAtributo", atributo == null ? null : atributo.getCodigo());
        metadata.put("tipoDato", atributo == null || atributo.getTipoDato() == null ? null : atributo.getTipoDato().getCode());
        metadata.put("valorTexto", valor.getValorTexto());
        metadata.put("valorNumero", valor.getValorNumero());
        metadata.put("valorBoolean", valor.getValorBoolean());
        metadata.put("valorFecha", valor.getValorFecha());
        metadata.put("estado", valor.getEstado());

        return metadata;
    }

    private boolean employeeCanUpdateAttributes(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeActualizarAtributos(actor.getIdUsuarioMs1());
    }

    private String validateEstadoChangeRequestAndReturnMotivo(EstadoChangeRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CAMBIO_ESTADO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del cambio de estado."
            );
        }

        if (!Boolean.FALSE.equals(request.estado())) {
            throw new ValidationException(
                    "SKU_ATRIBUTO_ESTADO_INVALIDO",
                    "Esta operación solo permite inactivar el valor de atributo del SKU."
            );
        }

        String motivo = StringNormalizer.cleanOrNull(request.motivo());

        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "MOTIVO_OBLIGATORIO",
                    "Debe indicar el motivo de la operación."
            );
        }

        if (motivo.length() > MAX_MOTIVO_LENGTH) {
            throw new ValidationException(
                    "MOTIVO_INVALIDO",
                    "El motivo no debe superar 500 caracteres."
            );
        }

        return motivo;
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private NotFoundException skuNotFound() {
        return new NotFoundException(
                "SKU_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }

    private NotFoundException atributoNotFound() {
        return new NotFoundException(
                "ATRIBUTO_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }

    private record ValorPreparado(
            SkuAtributoValorRequestDto request,
            Atributo atributo,
            TipoProductoAtributo relation
    ) {
    }
}