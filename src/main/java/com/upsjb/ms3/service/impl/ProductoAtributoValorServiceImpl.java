// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoAtributoValorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoAtributoValorFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.mapper.ProductoAtributoValorMapper;
import com.upsjb.ms3.policy.ProductoPolicy;
import com.upsjb.ms3.repository.AtributoRepository;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.ProductoAtributoValorService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProductoAtributoValorSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.AtributoValidator;
import com.upsjb.ms3.validator.ProductoAtributoValorValidator;
import com.upsjb.ms3.validator.ProductoValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoAtributoValorServiceImpl implements ProductoAtributoValorService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idProductoAtributoValor",
            "producto.codigoProducto",
            "producto.nombre",
            "producto.slug",
            "atributo.codigo",
            "atributo.nombre",
            "atributo.tipoDato",
            "valorTexto",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_CODIGO_PRODUCTO_LENGTH = 80;
    private static final int MAX_NOMBRE_PRODUCTO_LENGTH = 180;
    private static final int MAX_SLUG_PRODUCTO_LENGTH = 240;
    private static final int MAX_CODIGO_ATRIBUTO_LENGTH = 50;
    private static final int MAX_NOMBRE_ATRIBUTO_LENGTH = 120;
    private static final int MAX_VALOR_TEXTO_LENGTH = 500;
    private static final int MAX_MOTIVO_LENGTH = 500;

    private final ProductoRepository productoRepository;
    private final AtributoRepository atributoRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final ProductoAtributoValorMapper productoAtributoValorMapper;
    private final ProductoValidator productoValidator;
    private final AtributoValidator atributoValidator;
    private final ProductoAtributoValorValidator productoAtributoValorValidator;
    private final ProductoPolicy productoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            Long idProducto,
            ProductoAtributoValorRequestDto request
    ) {
        return guardarValor(EntityReferenceDto.builder().id(idProducto).build(), request);
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            EntityReferenceDto productoReference,
            ProductoAtributoValorRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        Producto producto = resolveProducto(productoReference);
        productoValidator.requireEditable(producto);

        ProductoAtributoValorRequestDto normalized = normalizeRequest(request);
        Atributo atributo = resolveAtributo(normalized.atributo());
        TipoProductoAtributo relation = resolveRelationRequired(producto, atributo);

        validateValue(producto, atributo, relation, normalized);

        ProductoAtributoValor entity = productoAtributoValorRepository
                .findByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(
                        producto.getIdProducto(),
                        atributo.getIdAtributo()
                )
                .orElse(null);

        boolean created = entity == null;
        Map<String, Object> before = created ? null : auditSnapshot(entity);

        if (created) {
            entity = productoAtributoValorMapper.toEntity(normalized, producto, atributo);
            entity.activar();
        } else {
            productoAtributoValorMapper.updateEntity(entity, normalized, atributo);
        }

        ProductoAtributoValor saved = productoAtributoValorRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_ATRIBUTO_VALOR,
                String.valueOf(saved.getIdProductoAtributoValor()),
                created ? "CREAR_VALOR_ATRIBUTO_PRODUCTO" : "ACTUALIZAR_VALOR_ATRIBUTO_PRODUCTO",
                created ? "Atributo de producto creado correctamente." : "Atributo de producto actualizado correctamente.",
                auditMetadata(saved, actor, before == null ? null : Map.of("before", before))
        );

        registrarOutboxProductoActualizado(producto);

        log.info(
                "Valor de atributo de producto guardado. idProducto={}, codigoProducto={}, idAtributo={}, actor={}",
                producto.getIdProducto(),
                producto.getCodigoProducto(),
                atributo.getIdAtributo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                created ? "Atributo de producto creado correctamente." : "Atributo de producto actualizado correctamente.",
                productoAtributoValorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            Long idProducto,
            List<ProductoAtributoValorRequestDto> request
    ) {
        return reemplazarValores(EntityReferenceDto.builder().id(idProducto).build(), request);
    }

    @Override
    @Transactional
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            EntityReferenceDto productoReference,
            List<ProductoAtributoValorRequestDto> request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        Producto producto = resolveProducto(productoReference);
        productoValidator.requireEditable(producto);

        List<TipoProductoAtributo> plantilla = findPlantillaActiva(producto);
        List<ValorPreparado> preparados = prepararValores(producto, request, plantilla);

        Set<Long> atributosProcesados = new LinkedHashSet<>();
        preparados.forEach(preparado -> atributosProcesados.add(preparado.atributo().getIdAtributo()));
        productoAtributoValorValidator.validateRequiredAttributesPresent(plantilla, atributosProcesados);

        List<ProductoAtributoValor> actuales = productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto());

        Map<Long, ProductoAtributoValor> actualesPorAtributo = new LinkedHashMap<>();
        for (ProductoAtributoValor actual : actuales) {
            if (actual.getAtributo() != null && actual.getAtributo().getIdAtributo() != null) {
                actualesPorAtributo.put(actual.getAtributo().getIdAtributo(), actual);
            }
        }

        List<ProductoAtributoValor> toPersist = new ArrayList<>();
        Set<Long> atributosReemplazados = new LinkedHashSet<>();

        for (ValorPreparado preparado : preparados) {
            Long idAtributo = preparado.atributo().getIdAtributo();
            atributosReemplazados.add(idAtributo);

            ProductoAtributoValor entity = actualesPorAtributo.get(idAtributo);
            if (entity == null) {
                entity = productoAtributoValorMapper.toEntity(preparado.request(), producto, preparado.atributo());
            } else {
                productoAtributoValorMapper.updateEntity(entity, preparado.request(), preparado.atributo());
            }

            entity.activar();
            toPersist.add(entity);
        }

        for (ProductoAtributoValor actual : actuales) {
            Long idAtributo = actual.getAtributo() == null ? null : actual.getAtributo().getIdAtributo();

            if (idAtributo != null && !atributosReemplazados.contains(idAtributo)) {
                actual.inactivar();
                toPersist.add(actual);
            }
        }

        List<ProductoAtributoValor> saved = productoAtributoValorRepository.saveAllAndFlush(toPersist);

        List<ProductoAtributoValorResponseDto> response = saved.stream()
                .filter(ProductoAtributoValor::isActivo)
                .map(productoAtributoValorMapper::toResponse)
                .toList();

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO,
                String.valueOf(producto.getIdProducto()),
                "REEMPLAZAR_ATRIBUTOS_PRODUCTO",
                "Atributos de producto reemplazados correctamente.",
                Map.of(
                        "idProducto", producto.getIdProducto(),
                        "codigoProducto", producto.getCodigoProducto(),
                        "cantidadAtributos", response.size(),
                        "actor", actor.actorLabel()
                )
        );

        registrarOutboxProductoActualizado(producto);

        log.info(
                "Atributos de producto reemplazados. idProducto={}, codigoProducto={}, cantidad={}, actor={}",
                producto.getIdProducto(),
                producto.getCodigoProducto(),
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
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(Long idProducto) {
        return listarPorProducto(EntityReferenceDto.builder().id(idProducto).build());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(EntityReferenceDto productoReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        Producto producto = resolveProducto(productoReference);

        List<ProductoAtributoValorResponseDto> response = productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .stream()
                .map(productoAtributoValorMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoAtributoValorResponseDto>> listar(
            ProductoAtributoValorFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");
        ProductoAtributoValorFilterDto normalizedFilter = normalizeFilter(filter);

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<ProductoAtributoValorResponseDto> response = paginationService.toPageResponseDto(
                productoAtributoValorRepository.findAll(
                        ProductoAtributoValorSpecifications.fromFilter(normalizedFilter),
                        pageable
                ),
                productoAtributoValorMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoAtributoValorResponseDto> obtenerDetalle(Long idProductoAtributoValor) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        ProductoAtributoValor entity = findAnyValorRequired(idProductoAtributoValor);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoAtributoValorMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoAtributoValorResponseDto> inactivar(
            Long idProductoAtributoValor,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));

        String motivo = validateEstadoChangeRequestAndReturnMotivo(request);

        ProductoAtributoValor entity = findActiveValorRequired(idProductoAtributoValor);
        Producto producto = entity.getProducto();
        productoValidator.requireEditable(producto);

        TipoProductoAtributo relation = resolveRelationOrNull(producto, entity.getAtributo());
        productoAtributoValorValidator.validateCanInactivate(entity, relation);

        Map<String, Object> before = auditSnapshot(entity);
        entity.inactivar();

        ProductoAtributoValor saved = productoAtributoValorRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_ATRIBUTO_VALOR,
                String.valueOf(saved.getIdProductoAtributoValor()),
                "INACTIVAR_VALOR_ATRIBUTO_PRODUCTO",
                "Atributo de producto inactivado correctamente.",
                auditMetadata(saved, actor, Map.of("before", before, "motivo", motivo))
        );

        registrarOutboxProductoActualizado(producto);

        log.info(
                "Valor de atributo de producto inactivado. idProductoAtributoValor={}, idProducto={}, actor={}",
                saved.getIdProductoAtributoValor(),
                producto == null ? null : producto.getIdProducto(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Atributo de producto inactivado correctamente.",
                productoAtributoValorMapper.toResponse(saved)
        );
    }

    private List<ValorPreparado> prepararValores(
            Producto producto,
            List<ProductoAtributoValorRequestDto> request,
            List<TipoProductoAtributo> plantilla
    ) {
        List<ProductoAtributoValorRequestDto> safeRequest = request == null ? List.of() : request;
        Set<Long> atributosProcesados = new LinkedHashSet<>();
        List<ValorPreparado> preparados = new ArrayList<>();

        for (ProductoAtributoValorRequestDto item : safeRequest) {
            ProductoAtributoValorRequestDto normalized = normalizeRequest(item);
            Atributo atributo = resolveAtributo(normalized.atributo());
            TipoProductoAtributo relation = findRelationFromTemplate(plantilla, atributo.getIdAtributo());

            productoAtributoValorValidator.validateDuplicateInReplacement(
                    atributo.getIdAtributo(),
                    atributosProcesados
            );
            validateValue(producto, atributo, relation, normalized);

            preparados.add(new ValorPreparado(normalized, atributo, relation));
        }

        return preparados;
    }

    private void validateValue(
            Producto producto,
            Atributo atributo,
            TipoProductoAtributo relation,
            ProductoAtributoValorRequestDto request
    ) {
        productoAtributoValorValidator.validateAssociationAllowed(producto, atributo, relation);

        atributoValidator.validateValueByType(
                atributo,
                request.valorTexto(),
                request.valorNumero(),
                request.valorBoolean(),
                request.valorFecha()
        );

        productoAtributoValorValidator.validateValueByTemplate(
                atributo,
                relation,
                request.valorTexto(),
                request.valorNumero(),
                request.valorBoolean(),
                request.valorFecha()
        );
    }

    private Producto resolveProducto(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "PRODUCTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el producto."
            );
        }

        if (reference.id() != null) {
            return productoRepository.findByIdProductoAndEstadoTrue(reference.id())
                    .orElseThrow(this::productoNotFound);
        }

        String codigoProducto = firstText(reference.codigoProducto(), reference.codigo());
        if (StringNormalizer.hasText(codigoProducto)) {
            return productoRepository.findByCodigoProductoIgnoreCaseAndEstadoTrue(codigoProducto)
                    .orElseThrow(this::productoNotFound);
        }

        if (StringNormalizer.hasText(reference.slug())) {
            return productoRepository.findBySlugIgnoreCaseAndEstadoTrue(reference.slug())
                    .orElseThrow(this::productoNotFound);
        }

        if (StringNormalizer.hasText(reference.nombre())) {
            return productoRepository.findByNombreIgnoreCaseAndEstadoTrue(reference.nombre())
                    .orElseThrow(this::productoNotFound);
        }

        throw new ValidationException(
                "PRODUCTO_REFERENCIA_INVALIDA",
                "Debe indicar id, codigoProducto, código, slug o nombre del producto."
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

    private ProductoAtributoValor findActiveValorRequired(Long idProductoAtributoValor) {
        if (idProductoAtributoValor == null) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_ID_REQUERIDO",
                    "Debe indicar el valor de atributo de producto."
            );
        }

        return productoAtributoValorRepository.findByIdProductoAtributoValorAndEstadoTrue(idProductoAtributoValor)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCTO_ATRIBUTO_VALOR_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private ProductoAtributoValor findAnyValorRequired(Long idProductoAtributoValor) {
        if (idProductoAtributoValor == null) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_ID_REQUERIDO",
                    "Debe indicar el valor de atributo de producto."
            );
        }

        return productoAtributoValorRepository.findByIdProductoAtributoValor(idProductoAtributoValor)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCTO_ATRIBUTO_VALOR_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private ProductoAtributoValorRequestDto normalizeRequest(ProductoAtributoValorRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_REQUEST_REQUERIDO",
                    "Debe enviar el valor del atributo."
            );
        }

        return ProductoAtributoValorRequestDto.builder()
                .atributo(request.atributo())
                .valorTexto(StringNormalizer.cleanOrNull(request.valorTexto()))
                .valorNumero(request.valorNumero())
                .valorBoolean(request.valorBoolean())
                .valorFecha(request.valorFecha())
                .build();
    }

    private ProductoAtributoValorFilterDto normalizeFilter(ProductoAtributoValorFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return ProductoAtributoValorFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .idProducto(filter.idProducto())
                .codigoProducto(StringNormalizer.truncateOrNull(filter.codigoProducto(), MAX_CODIGO_PRODUCTO_LENGTH))
                .nombreProducto(StringNormalizer.truncateOrNull(filter.nombreProducto(), MAX_NOMBRE_PRODUCTO_LENGTH))
                .slugProducto(StringNormalizer.truncateOrNull(filter.slugProducto(), MAX_SLUG_PRODUCTO_LENGTH))
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

    private TipoProductoAtributo resolveRelationRequired(Producto producto, Atributo atributo) {
        return resolveRelationOrNull(producto, atributo);
    }

    private TipoProductoAtributo resolveRelationOrNull(Producto producto, Atributo atributo) {
        productoAtributoValorValidator.requireProductWithProductType(producto);

        if (atributo == null || atributo.getIdAtributo() == null) {
            return null;
        }

        Long idTipoProducto = producto.getTipoProducto().getIdTipoProducto();

        return tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        idTipoProducto,
                        atributo.getIdAtributo()
                )
                .orElse(null);
    }

    private List<TipoProductoAtributo> findPlantillaActiva(Producto producto) {
        productoAtributoValorValidator.requireProductWithProductType(producto);

        return tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
                        producto.getTipoProducto().getIdTipoProducto()
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

    private void registrarOutboxProductoActualizado(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return;
        }

        AuditContext context = AuditContextHolder.getOrEmpty();

        ProductoSnapshotEvent event = ProductoSnapshotEvent.of(
                ProductoEventType.PRODUCTO_SNAPSHOT_ACTUALIZADO,
                producto.getIdProducto(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toProductoSnapshotPayload(producto),
                Map.of("source", "ProductoAtributoValorService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private ProductoSnapshotPayload toProductoSnapshotPayload(Producto producto) {
        TipoProducto tipoProducto = producto.getTipoProducto();
        Categoria categoria = producto.getCategoria();
        Marca marca = producto.getMarca();

        List<ProductoSnapshotPayload.ProductoAtributoSnapshotPayload> atributos = productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .stream()
                .map(this::toProductoAtributoSnapshotPayload)
                .toList();

        return ProductoSnapshotPayload.builder()
                .idProducto(producto.getIdProducto())
                .codigoProducto(producto.getCodigoProducto())
                .nombre(producto.getNombre())
                .slug(producto.getSlug())
                .idTipoProducto(tipoProducto == null ? null : tipoProducto.getIdTipoProducto())
                .codigoTipoProducto(tipoProducto == null ? null : tipoProducto.getCodigo())
                .nombreTipoProducto(tipoProducto == null ? null : tipoProducto.getNombre())
                .idCategoria(categoria == null ? null : categoria.getIdCategoria())
                .codigoCategoria(categoria == null ? null : categoria.getCodigo())
                .nombreCategoria(categoria == null ? null : categoria.getNombre())
                .slugCategoria(categoria == null ? null : categoria.getSlug())
                .idMarca(marca == null ? null : marca.getIdMarca())
                .codigoMarca(marca == null ? null : marca.getCodigo())
                .nombreMarca(marca == null ? null : marca.getNombre())
                .slugMarca(marca == null ? null : marca.getSlug())
                .descripcionCorta(producto.getDescripcionCorta())
                .descripcionLarga(producto.getDescripcionLarga())
                .generoObjetivo(producto.getGeneroObjetivo() == null ? null : producto.getGeneroObjetivo().getCode())
                .temporada(producto.getTemporada())
                .deporte(producto.getDeporte())
                .estadoRegistro(producto.getEstadoRegistro() == null ? null : producto.getEstadoRegistro().getCode())
                .estadoPublicacion(producto.getEstadoPublicacion() == null ? null : producto.getEstadoPublicacion().getCode())
                .estadoVenta(producto.getEstadoVenta() == null ? null : producto.getEstadoVenta().getCode())
                .visiblePublico(producto.getVisiblePublico())
                .vendible(producto.getVendible())
                .fechaPublicacionInicio(producto.getFechaPublicacionInicio())
                .fechaPublicacionFin(producto.getFechaPublicacionFin())
                .motivoEstado(producto.getMotivoEstado())
                .estado(producto.getEstado())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .atributos(atributos)
                .build();
    }

    private ProductoSnapshotPayload.ProductoAtributoSnapshotPayload toProductoAtributoSnapshotPayload(
            ProductoAtributoValor valor
    ) {
        Atributo atributo = valor.getAtributo();
        Producto producto = valor.getProducto();
        TipoDatoAtributo tipoDato = atributo == null ? null : atributo.getTipoDato();

        return ProductoSnapshotPayload.ProductoAtributoSnapshotPayload.builder()
                .idProductoAtributoValor(valor.getIdProductoAtributoValor())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .idAtributo(atributo == null ? null : atributo.getIdAtributo())
                .codigoAtributo(atributo == null ? null : atributo.getCodigo())
                .nombreAtributo(atributo == null ? null : atributo.getNombre())
                .tipoDato(tipoDato == null ? null : tipoDato.getCode())
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
                .requerido(atributo == null ? null : atributo.getRequerido())
                .filtrable(atributo == null ? null : atributo.getFiltrable())
                .visiblePublico(atributo == null ? null : atributo.getVisiblePublico())
                .valorTexto(valor.getValorTexto())
                .valorNumero(valor.getValorNumero())
                .valorBoolean(valor.getValorBoolean())
                .valorFecha(valor.getValorFecha())
                .valorDisplay(valorDisplay(valor))
                .estado(valor.getEstado())
                .createdAt(valor.getCreatedAt())
                .updatedAt(valor.getUpdatedAt())
                .build();
    }

    private Map<String, Object> auditMetadata(
            ProductoAtributoValor valor,
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

    private Map<String, Object> auditSnapshot(ProductoAtributoValor valor) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        Producto producto = valor.getProducto();
        Atributo atributo = valor.getAtributo();

        metadata.put("idProductoAtributoValor", valor.getIdProductoAtributoValor());
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
                    "PRODUCTO_ATRIBUTO_ESTADO_INVALIDO",
                    "Esta operación solo permite inactivar el valor de atributo del producto."
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

    private String valorDisplay(ProductoAtributoValor valor) {
        if (valor == null || valor.getAtributo() == null || valor.getAtributo().getTipoDato() == null) {
            return null;
        }

        return switch (valor.getAtributo().getTipoDato()) {
            case TEXTO -> valor.getValorTexto();
            case NUMERO, DECIMAL -> valor.getValorNumero() == null ? null : valor.getValorNumero().toPlainString();
            case BOOLEANO -> valor.getValorBoolean() == null ? null : valor.getValorBoolean().toString();
            case FECHA -> valor.getValorFecha() == null ? null : valor.getValorFecha().toString();
        };
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }

    private NotFoundException productoNotFound() {
        return new NotFoundException(
                "PRODUCTO_NO_ENCONTRADO",
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
            ProductoAtributoValorRequestDto request,
            Atributo atributo,
            TipoProductoAtributo relation
    ) {
    }
}