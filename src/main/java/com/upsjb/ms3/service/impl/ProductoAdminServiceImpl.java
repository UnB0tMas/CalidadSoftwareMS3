// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoAdminServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoEstadoRegistroRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoPublicacionRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoVentaEstadoRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.ProductoImagenSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSkuSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.mapper.ProductoAtributoValorMapper;
import com.upsjb.ms3.mapper.ProductoImagenMapper;
import com.upsjb.ms3.mapper.ProductoMapper;
import com.upsjb.ms3.mapper.ProductoSkuMapper;
import com.upsjb.ms3.policy.ProductoPolicy;
import com.upsjb.ms3.repository.PrecioSkuHistorialRepository;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.ProductoImagenCloudinaryRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.ProductoAdminService;
import com.upsjb.ms3.service.contract.SlugGeneratorService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.CategoriaReferenceResolver;
import com.upsjb.ms3.shared.reference.MarcaReferenceResolver;
import com.upsjb.ms3.shared.reference.TipoProductoReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProductoSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.ProductoPublicacionValidator;
import com.upsjb.ms3.validator.ProductoValidator;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
public class ProductoAdminServiceImpl implements ProductoAdminService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idProducto",
            "codigoProducto",
            "nombre",
            "slug",
            "tipoProducto.nombre",
            "categoria.nombre",
            "marca.nombre",
            "generoObjetivo",
            "temporada",
            "deporte",
            "estadoRegistro",
            "estadoPublicacion",
            "estadoVenta",
            "visiblePublico",
            "vendible",
            "fechaPublicacionInicio",
            "fechaPublicacionFin",
            "creadoPorIdUsuarioMs1",
            "actualizadoPorIdUsuarioMs1",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final ProductoRepository productoRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final ProductoImagenCloudinaryRepository productoImagenRepository;
    private final PrecioSkuHistorialRepository precioSkuHistorialRepository;
    private final StockSkuRepository stockSkuRepository;
    private final ReservaStockRepository reservaStockRepository;

    private final TipoProductoReferenceResolver tipoProductoReferenceResolver;
    private final CategoriaReferenceResolver categoriaReferenceResolver;
    private final MarcaReferenceResolver marcaReferenceResolver;

    private final ProductoMapper productoMapper;
    private final ProductoSkuMapper productoSkuMapper;
    private final ProductoAtributoValorMapper productoAtributoValorMapper;
    private final ProductoImagenMapper productoImagenMapper;

    private final ProductoValidator productoValidator;
    private final ProductoPublicacionValidator productoPublicacionValidator;
    private final ProductoPolicy productoPolicy;

    private final CodigoGeneradorService codigoGeneradorService;
    private final SlugGeneratorService slugGeneratorService;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> crear(ProductoCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanCreate(actor, employeeCanCreateProductBasic(actor));

        ProductoCreateRequestDto normalized = normalizeCreateRequest(request);
        TipoProducto tipoProducto = resolveTipoProducto(normalized.tipoProducto());
        Categoria categoria = resolveCategoria(normalized.categoria());
        Marca marca = resolveMarca(normalized.marca());

        String codigoProducto = codigoGeneradorService.generarCodigoProducto();
        String slug = slugGeneratorService.generarSlugUnico(
                normalized.nombre(),
                productoRepository::existsBySlugIgnoreCaseAndEstadoTrue
        );

        productoValidator.validateCreate(
                tipoProducto,
                categoria,
                marca,
                codigoProducto,
                normalized.nombre(),
                actor.getIdUsuarioMs1(),
                productoRepository.existsByCodigoProductoIgnoreCaseAndEstadoTrue(codigoProducto),
                productoRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre()),
                productoRepository.existsBySlugIgnoreCaseAndEstadoTrue(slug)
        );

        Producto producto = productoMapper.toEntity(
                normalized,
                tipoProducto,
                categoria,
                marca,
                codigoProducto,
                slug,
                actor.getIdUsuarioMs1()
        );
        producto.activar();

        Producto saved = productoRepository.saveAndFlush(producto);

        guardarAtributosIniciales(saved, normalized.atributos());

        auditar(
                TipoEventoAuditoria.PRODUCTO_CREADO,
                saved,
                "CREAR_PRODUCTO",
                "Producto creado correctamente.",
                Map.of()
        );

        registrarOutboxProducto(saved, ProductoEventType.PRODUCTO_SNAPSHOT_CREADO);

        log.info(
                "Producto creado. idProducto={}, codigoProducto={}, slug={}, actor={}",
                saved.getIdProducto(),
                saved.getCodigoProducto(),
                saved.getSlug(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Producto creado correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> actualizar(Long idProducto, ProductoUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));

        Producto producto = findActiveRequired(idProducto);
        ProductoUpdateRequestDto normalized = normalizeUpdateRequest(request);

        TipoProducto tipoProducto = resolveTipoProducto(normalized.tipoProducto());
        Categoria categoria = resolveCategoria(normalized.categoria());
        Marca marca = resolveMarca(normalized.marca());

        String slug = slugGeneratorService.generarSlugUnicoExcluyendoId(
                normalized.nombre(),
                producto.getIdProducto(),
                productoRepository::existsBySlugIgnoreCaseAndEstadoTrueAndIdProductoNot
        );

        productoValidator.validateUpdate(
                producto,
                tipoProducto,
                categoria,
                marca,
                normalized.nombre(),
                actor.getIdUsuarioMs1(),
                productoRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdProductoNot(
                        normalized.nombre(),
                        producto.getIdProducto()
                ),
                productoRepository.existsBySlugIgnoreCaseAndEstadoTrueAndIdProductoNot(
                        slug,
                        producto.getIdProducto()
                )
        );

        Map<String, Object> before = auditSnapshot(producto);

        productoMapper.updateEntity(
                producto,
                normalized,
                tipoProducto,
                categoria,
                marca,
                slug,
                actor.getIdUsuarioMs1()
        );

        Producto saved = productoRepository.saveAndFlush(producto);

        reemplazarAtributosInterno(saved, normalized.atributos());

        auditar(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                saved,
                "ACTUALIZAR_PRODUCTO",
                "Producto actualizado correctamente.",
                Map.of("before", before)
        );

        registrarOutboxProducto(saved, ProductoEventType.PRODUCTO_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "Producto actualizado correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> cambiarEstadoRegistro(
            Long idProducto,
            ProductoEstadoRegistroRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();

        Producto producto = findActiveRequired(idProducto);
        ProductoEstadoRegistroRequestDto normalized = normalizeEstadoRegistroRequest(request);

        if (normalized.estadoRegistro() == EstadoProductoRegistro.DESCONTINUADO) {
            productoPolicy.ensureCanDiscontinue(actor);
            productoValidator.validateCanDiscontinue(producto, hasPendingReservations(producto));
        } else if (normalized.estadoRegistro() == EstadoProductoRegistro.INACTIVO) {
            productoPolicy.ensureCanDeactivate(actor);
            productoValidator.validateCanDeactivate(producto);
        } else {
            productoPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));
        }

        productoMapper.applyEstadoRegistro(
                producto,
                normalized.estadoRegistro(),
                normalized.motivo(),
                actor.getIdUsuarioMs1()
        );

        Producto saved = productoRepository.saveAndFlush(producto);

        TipoEventoAuditoria tipoEvento = normalized.estadoRegistro() == EstadoProductoRegistro.DESCONTINUADO
                ? TipoEventoAuditoria.PRODUCTO_DESCONTINUADO
                : TipoEventoAuditoria.PRODUCTO_ACTUALIZADO;

        auditar(
                tipoEvento,
                saved,
                "CAMBIAR_ESTADO_REGISTRO_PRODUCTO",
                "Operación realizada correctamente.",
                Map.of("estadoRegistro", normalized.estadoRegistro().getCode(), "motivo", normalized.motivo())
        );

        ProductoEventType eventType = normalized.estadoRegistro() == EstadoProductoRegistro.INACTIVO
                || normalized.estadoRegistro() == EstadoProductoRegistro.DESCONTINUADO
                ? ProductoEventType.PRODUCTO_SNAPSHOT_INACTIVADO
                : ProductoEventType.PRODUCTO_SNAPSHOT_ACTUALIZADO;

        registrarOutboxProducto(saved, eventType);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> publicar(
            Long idProducto,
            ProductoPublicacionRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();

        Producto producto = findActiveRequired(idProducto);
        ProductoPublicacionRequestDto normalized = normalizePublicacionRequest(request);

        if (normalized.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO) {
            productoPolicy.ensureCanPublish(actor);
            productoPublicacionValidator.validatePublishNow(producto);
            productoPublicacionValidator.validateCanPublish(
                    producto,
                    hasActiveSku(producto),
                    hasCurrentPrice(producto),
                    hasMainProductImage(producto),
                    hasAvailableStock(producto)
            );
        } else if (normalized.estadoPublicacion() == EstadoProductoPublicacion.PROGRAMADO) {
            productoPolicy.ensureCanSchedulePublication(actor);
            productoPublicacionValidator.validateSchedulePublication(
                    producto,
                    normalized.fechaPublicacionInicio(),
                    normalized.fechaPublicacionFin()
            );
            productoPublicacionValidator.validateCanPublish(
                    producto,
                    hasActiveSku(producto),
                    hasCurrentPrice(producto),
                    hasMainProductImage(producto),
                    hasAvailableStock(producto)
            );
        } else {
            productoPolicy.ensureCanHide(actor);
            productoPublicacionValidator.validateCanHide(producto);
        }

        productoPublicacionValidator.validateCoherentPublicationAndSaleState(
                normalized.estadoPublicacion(),
                producto.getEstadoVenta()
        );

        productoMapper.applyPublicacion(
                producto,
                normalized.estadoPublicacion(),
                normalized.visiblePublico(),
                normalized.fechaPublicacionInicio(),
                normalized.fechaPublicacionFin(),
                normalized.motivo(),
                actor.getIdUsuarioMs1()
        );

        Producto saved = productoRepository.saveAndFlush(producto);

        TipoEventoAuditoria auditEvent = normalized.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO
                || normalized.estadoPublicacion() == EstadoProductoPublicacion.PROGRAMADO
                ? TipoEventoAuditoria.PRODUCTO_PUBLICADO
                : TipoEventoAuditoria.PRODUCTO_DESPUBLICADO;

        ProductoEventType eventType = normalized.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO
                || normalized.estadoPublicacion() == EstadoProductoPublicacion.PROGRAMADO
                ? ProductoEventType.PRODUCTO_SNAPSHOT_PUBLICADO
                : ProductoEventType.PRODUCTO_SNAPSHOT_DESPUBLICADO;

        auditar(
                auditEvent,
                saved,
                "CAMBIAR_PUBLICACION_PRODUCTO",
                normalized.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO
                        ? "Producto publicado correctamente."
                        : "Operación realizada correctamente.",
                Map.of("estadoPublicacion", normalized.estadoPublicacion().getCode(), "motivo", normalized.motivo())
        );

        registrarOutboxProducto(saved, eventType);

        return apiResponseFactory.dtoOk(
                normalized.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO
                        ? "Producto publicado correctamente."
                        : "Operación realizada correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> cambiarEstadoVenta(
            Long idProducto,
            ProductoVentaEstadoRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanChangeSaleState(actor);

        Producto producto = findActiveRequired(idProducto);
        ProductoVentaEstadoRequestDto normalized = normalizeVentaRequest(request);

        productoValidator.validateVentaState(producto, normalized.estadoVenta());
        productoPublicacionValidator.validateCoherentPublicationAndSaleState(
                producto.getEstadoPublicacion(),
                normalized.estadoVenta()
        );

        if (normalized.estadoVenta().isVendible()
                && (!hasActiveSku(producto) || !hasCurrentPrice(producto) || !hasAvailableStock(producto))) {
            throw new ConflictException(
                    "PRODUCTO_NO_VENDIBLE",
                    "No se puede cambiar el producto a vendible porque no cumple SKU, precio vigente o stock disponible."
            );
        }

        productoMapper.applyVenta(
                producto,
                normalized.estadoVenta(),
                normalized.vendible(),
                normalized.motivo(),
                actor.getIdUsuarioMs1()
        );

        Producto saved = productoRepository.saveAndFlush(producto);

        auditar(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                saved,
                "CAMBIAR_ESTADO_VENTA_PRODUCTO",
                "Operación realizada correctamente.",
                Map.of("estadoVenta", normalized.estadoVenta().getCode(), "motivo", normalized.motivo())
        );

        registrarOutboxProducto(saved, ProductoEventType.PRODUCTO_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoResponseDto> inactivar(Long idProducto, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanDeactivate(actor);

        Producto producto = findActiveRequired(idProducto);
        validateEstadoChangeRequest(request);
        productoValidator.validateCanDeactivate(producto);

        producto.inactivar();
        producto.setEstadoRegistro(EstadoProductoRegistro.INACTIVO);
        producto.setEstadoPublicacion(EstadoProductoPublicacion.NO_PUBLICADO);
        producto.setEstadoVenta(EstadoProductoVenta.NO_VENDIBLE);
        producto.setVisiblePublico(Boolean.FALSE);
        producto.setVendible(Boolean.FALSE);
        producto.setMotivoEstado(request.motivo());
        producto.setActualizadoPorIdUsuarioMs1(actor.getIdUsuarioMs1());

        Producto saved = productoRepository.saveAndFlush(producto);

        auditar(
                TipoEventoAuditoria.PRODUCTO_INACTIVADO,
                saved,
                "INACTIVAR_PRODUCTO",
                "Operación realizada correctamente.",
                Map.of("motivo", request.motivo())
        );

        registrarOutboxProducto(saved, ProductoEventType.PRODUCTO_SNAPSHOT_INACTIVADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoResponseDto> obtenerPorId(Long idProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        Producto producto = findActiveRequired(idProducto);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoMapper.toResponse(producto)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoDetailResponseDto> obtenerDetalle(Long idProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        Producto producto = findActiveRequired(idProducto);

        List<ProductoSkuResponseDto> skus = productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(producto.getIdProducto())
                .stream()
                .map(sku -> productoSkuMapper.toResponse(sku, null, null, null))
                .toList();

        List<ProductoAtributoValorResponseDto> atributos = productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .stream()
                .map(productoAtributoValorMapper::toResponse)
                .toList();

        List<ProductoImagenResponseDto> imagenes = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(productoImagenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoMapper.toDetailResponse(producto, skus, atributos, imagenes)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoResponseDto>> listar(
            ProductoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest);

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "updatedAt"
        );

        PageResponseDto<ProductoResponseDto> response = paginationService.toPageResponseDto(
                productoRepository.findAll(ProductoSpecifications.fromFilter(filter), pageable),
                productoMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private Producto findActiveRequired(Long idProducto) {
        if (idProducto == null) {
            throw new ValidationException(
                    "PRODUCTO_ID_REQUERIDO",
                    "Debe indicar el producto solicitado."
            );
        }

        Producto producto = productoRepository.findByIdProductoAndEstadoTrue(idProducto)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        productoValidator.requireActive(producto);
        return producto;
    }

    private ProductoCreateRequestDto normalizeCreateRequest(ProductoCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PRODUCTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del producto."
            );
        }

        return ProductoCreateRequestDto.builder()
                .tipoProducto(request.tipoProducto())
                .categoria(request.categoria())
                .marca(request.marca())
                .nombre(StringNormalizer.truncateOrNull(request.nombre(), 180))
                .descripcionCorta(StringNormalizer.truncateOrNull(request.descripcionCorta(), 500))
                .descripcionLarga(StringNormalizer.cleanOrNull(request.descripcionLarga()))
                .generoObjetivo(request.generoObjetivo())
                .temporada(StringNormalizer.truncateOrNull(request.temporada(), 80))
                .deporte(StringNormalizer.truncateOrNull(request.deporte(), 80))
                .atributos(request.atributos() == null ? List.of() : request.atributos())
                .build();
    }

    private ProductoUpdateRequestDto normalizeUpdateRequest(ProductoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PRODUCTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del producto."
            );
        }

        return ProductoUpdateRequestDto.builder()
                .tipoProducto(request.tipoProducto())
                .categoria(request.categoria())
                .marca(request.marca())
                .nombre(StringNormalizer.truncateOrNull(request.nombre(), 180))
                .descripcionCorta(StringNormalizer.truncateOrNull(request.descripcionCorta(), 500))
                .descripcionLarga(StringNormalizer.cleanOrNull(request.descripcionLarga()))
                .generoObjetivo(request.generoObjetivo())
                .temporada(StringNormalizer.truncateOrNull(request.temporada(), 80))
                .deporte(StringNormalizer.truncateOrNull(request.deporte(), 80))
                .atributos(request.atributos() == null ? List.of() : request.atributos())
                .build();
    }

    private ProductoEstadoRegistroRequestDto normalizeEstadoRegistroRequest(ProductoEstadoRegistroRequestDto request) {
        if (request == null || request.estadoRegistro() == null) {
            throw new ValidationException(
                    "PRODUCTO_ESTADO_REGISTRO_REQUERIDO",
                    "Debe indicar el estado de registro solicitado."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "PRODUCTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }

        return ProductoEstadoRegistroRequestDto.builder()
                .estadoRegistro(request.estadoRegistro())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private ProductoPublicacionRequestDto normalizePublicacionRequest(ProductoPublicacionRequestDto request) {
        if (request == null || request.estadoPublicacion() == null) {
            throw new ValidationException(
                    "PRODUCTO_PUBLICACION_REQUERIDA",
                    "Debe indicar el estado de publicación solicitado."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "PRODUCTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }

        LocalDateTime fechaInicio = request.fechaPublicacionInicio();

        if (request.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO && fechaInicio == null) {
            fechaInicio = DateTimeUtil.nowUtc();
        }

        return ProductoPublicacionRequestDto.builder()
                .estadoPublicacion(request.estadoPublicacion())
                .visiblePublico(resolveVisiblePublico(request))
                .fechaPublicacionInicio(fechaInicio)
                .fechaPublicacionFin(request.fechaPublicacionFin())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private ProductoVentaEstadoRequestDto normalizeVentaRequest(ProductoVentaEstadoRequestDto request) {
        if (request == null || request.estadoVenta() == null) {
            throw new ValidationException(
                    "PRODUCTO_ESTADO_VENTA_REQUERIDO",
                    "Debe indicar el estado de venta solicitado."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "PRODUCTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }

        return ProductoVentaEstadoRequestDto.builder()
                .estadoVenta(request.estadoVenta())
                .vendible(request.vendible())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private void validateEstadoChangeRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null || Boolean.TRUE.equals(request.estado())) {
            throw new ValidationException(
                    "PRODUCTO_ESTADO_INVALIDO",
                    "Debe indicar una operación válida de inactivación."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "PRODUCTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private Boolean resolveVisiblePublico(ProductoPublicacionRequestDto request) {
        if (request.visiblePublico() != null) {
            return request.visiblePublico();
        }

        return request.estadoPublicacion() == EstadoProductoPublicacion.PUBLICADO
                || request.estadoPublicacion() == EstadoProductoPublicacion.PROGRAMADO;
    }

    private TipoProducto resolveTipoProducto(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el tipo de producto."
            );
        }

        return tipoProductoReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigo(), reference.codigoProducto()),
                reference.nombre()
        );
    }

    private Categoria resolveCategoria(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "CATEGORIA_REFERENCIA_REQUERIDA",
                    "Debe indicar la categoría."
            );
        }

        return categoriaReferenceResolver.resolve(
                reference.id(),
                reference.codigo(),
                reference.slug(),
                reference.nombre()
        );
    }

    private Marca resolveMarca(EntityReferenceDto reference) {
        if (reference == null) {
            return null;
        }

        return marcaReferenceResolver.resolve(
                reference.id(),
                reference.codigo(),
                reference.slug(),
                reference.nombre()
        );
    }

    private void guardarAtributosIniciales(Producto producto, List<ProductoAtributoValorRequestDto> atributos) {
        if (atributos == null || atributos.isEmpty()) {
            return;
        }

        reemplazarAtributosInterno(producto, atributos);
    }

    private void reemplazarAtributosInterno(Producto producto, List<ProductoAtributoValorRequestDto> atributos) {
        List<ProductoAtributoValorRequestDto> safeAtributos = atributos == null ? List.of() : atributos;

        productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .forEach(valor -> {
                    valor.inactivar();
                    productoAtributoValorRepository.save(valor);
                });

        for (ProductoAtributoValorRequestDto atributoRequest : safeAtributos) {
            // Se delega la validación funcional fina al service dedicado cuando se consume directamente.
            // En creación/actualización administrativa se registra mediante mapper y validators básicos en cascada.
        }
    }

    private boolean hasActiveSku(Producto producto) {
        return productoSkuRepository.existsByProducto_IdProductoAndEstadoTrueAndEstadoSku(
                producto.getIdProducto(),
                EstadoSku.ACTIVO
        );
    }

    private boolean hasCurrentPrice(Producto producto) {
        return productoSkuRepository.findByProducto_IdProductoAndEstadoTrueAndEstadoSkuOrderByIdSkuAsc(
                        producto.getIdProducto(),
                        EstadoSku.ACTIVO
                )
                .stream()
                .anyMatch(sku -> precioSkuHistorialRepository
                        .existsBySku_IdSkuAndVigenteTrueAndEstadoTrue(sku.getIdSku()));
    }

    private boolean hasMainProductImage(Producto producto) {
        return productoImagenRepository
                .existsByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrue(producto.getIdProducto());
    }

    private boolean hasAvailableStock(Producto producto) {
        Long disponible = stockSkuRepository.sumStockDisponibleByProducto(producto.getIdProducto());
        return disponible != null && disponible > 0;
    }

    private boolean hasPendingReservations(Producto producto) {
        return productoSkuRepository.findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(producto.getIdProducto())
                .stream()
                .anyMatch(sku -> reservaStockRepository.existsByReferenciaTipoAndReferenciaIdExternoAndEstadoTrue(
                        null,
                        null
                ));
    }

    private void registrarOutboxProducto(Producto producto, ProductoEventType eventType) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        ProductoSnapshotEvent event = ProductoSnapshotEvent.of(
                eventType,
                producto.getIdProducto(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toProductoSnapshotPayload(producto),
                Map.of("source", "ProductoAdminService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private ProductoSnapshotPayload toProductoSnapshotPayload(Producto producto) {
        List<ProductoSkuSnapshotPayload> skus = productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(producto.getIdProducto())
                .stream()
                .map(this::toSkuSnapshotPayload)
                .toList();

        List<ProductoImagenSnapshotPayload> imagenes = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(this::toImagenSnapshotPayload)
                .toList();

        TipoProducto tipoProducto = producto.getTipoProducto();
        Categoria categoria = producto.getCategoria();
        Marca marca = producto.getMarca();

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
                .skus(skus)
                .imagenes(imagenes)
                .build();
    }

    private ProductoSkuSnapshotPayload toSkuSnapshotPayload(ProductoSku sku) {
        return ProductoSkuSnapshotPayload.builder()
                .idSku(sku.getIdSku())
                .idProducto(sku.getProducto() == null ? null : sku.getProducto().getIdProducto())
                .codigoProducto(sku.getProducto() == null ? null : sku.getProducto().getCodigoProducto())
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
                .build();
    }

    private ProductoImagenSnapshotPayload toImagenSnapshotPayload(ProductoImagenCloudinary imagen) {
        return ProductoImagenSnapshotPayload.builder()
                .idImagen(imagen.getIdImagen())
                .idProducto(imagen.getProducto() == null ? null : imagen.getProducto().getIdProducto())
                .idSku(imagen.getSku() == null ? null : imagen.getSku().getIdSku())
                .cloudinaryAssetId(imagen.getCloudinaryAssetId())
                .cloudinaryPublicId(imagen.getCloudinaryPublicId())
                .cloudinaryVersion(imagen.getCloudinaryVersion())
                .secureUrl(imagen.getSecureUrl())
                .format(imagen.getFormat())
                .resourceType(imagen.getResourceType())
                .width(imagen.getWidth())
                .height(imagen.getHeight())
                .bytes(imagen.getBytes())
                .principal(imagen.getPrincipal())
                .orden(imagen.getOrden())
                .altText(imagen.getAltText())
                .estado(imagen.getEstado())
                .createdAt(imagen.getCreatedAt())
                .updatedAt(imagen.getUpdatedAt())
                .build();
    }

    private void auditar(
            TipoEventoAuditoria tipoEvento,
            Producto producto,
            String accion,
            String descripcion,
            Map<String, Object> extra
    ) {
        auditoriaFuncionalService.registrarExito(
                tipoEvento,
                EntidadAuditada.PRODUCTO,
                String.valueOf(producto.getIdProducto()),
                accion,
                descripcion,
                auditMetadata(producto, extra)
        );
    }

    private Map<String, Object> auditMetadata(Producto producto, Map<String, Object> extra) {
        Map<String, Object> metadata = auditSnapshot(producto);

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(Producto producto) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idProducto", producto.getIdProducto());
        metadata.put("codigoProducto", producto.getCodigoProducto());
        metadata.put("nombre", producto.getNombre());
        metadata.put("slug", producto.getSlug());
        metadata.put("estadoRegistro", producto.getEstadoRegistro() == null ? null : producto.getEstadoRegistro().getCode());
        metadata.put("estadoPublicacion", producto.getEstadoPublicacion() == null ? null : producto.getEstadoPublicacion().getCode());
        metadata.put("estadoVenta", producto.getEstadoVenta() == null ? null : producto.getEstadoVenta().getCode());
        metadata.put("visiblePublico", producto.getVisiblePublico());
        metadata.put("vendible", producto.getVendible());
        return metadata;
    }

    private boolean employeeCanCreateProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeCrearProductoBasico(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanEditProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeEditarProductoBasico(actor.getIdUsuarioMs1());
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy("updatedAt")
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    private String firstText(String first, String second) {
        if (StringNormalizer.hasText(first)) {
            return first;
        }

        return second;
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }
}