// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoImagenServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoImagenFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenPrincipalRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUploadRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.integration.cloudinary.CloudinaryUploadResponse;
import com.upsjb.ms3.kafka.event.ProductoImagenSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSkuSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.mapper.ProductoImagenMapper;
import com.upsjb.ms3.policy.ProductoImagenPolicy;
import com.upsjb.ms3.repository.ProductoImagenCloudinaryRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CloudinaryService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.ProductoImagenService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.ProductoReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProductoImagenSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CloudinaryImageValidator;
import com.upsjb.ms3.validator.ProductoImagenValidator;
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
public class ProductoImagenServiceImpl implements ProductoImagenService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idImagen",
            "producto.codigoProducto",
            "producto.nombre",
            "sku.codigoSku",
            "cloudinaryPublicId",
            "cloudinaryAssetId",
            "resourceType",
            "format",
            "bytes",
            "width",
            "height",
            "orden",
            "principal",
            "creadoPorIdUsuarioMs1",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final ProductoImagenCloudinaryRepository productoImagenRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final ProductoReferenceResolver productoReferenceResolver;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final ProductoImagenMapper productoImagenMapper;
    private final ProductoImagenValidator productoImagenValidator;
    private final CloudinaryImageValidator cloudinaryImageValidator;
    private final ProductoImagenPolicy productoImagenPolicy;
    private final CloudinaryService cloudinaryService;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProductoImagenResponseDto> subir(ProductoImagenUploadRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanUpload(actor, employeeCanManageImages(actor));

        ProductoImagenUploadRequestDto normalized = normalizeUploadRequest(request);
        Producto producto = resolveProducto(normalized.producto());
        ProductoSku sku = resolveOptionalSku(normalized.sku());

        validateSkuBelongsToProduct(producto, sku);

        if (Boolean.TRUE.equals(normalized.principal())) {
            clearPrincipal(producto, sku);
        }

        productoImagenValidator.validateCreate(
                producto,
                sku,
                normalized.principal(),
                normalized.orden(),
                false,
                false
        );

        cloudinaryImageValidator.validateUploadFile(normalized.archivo());

        CloudinaryUploadResponse cloudinaryResponse = sku == null
                ? cloudinaryService.subirImagenProducto(
                normalized.archivo(),
                producto.getCodigoProducto(),
                uploadMetadata(producto, null),
                uploadTags(producto, null)
        )
                : cloudinaryService.subirImagenSku(
                normalized.archivo(),
                producto.getCodigoProducto(),
                sku.getCodigoSku(),
                uploadMetadata(producto, sku),
                uploadTags(producto, sku)
        );

        try {
            productoImagenValidator.validateMetadata(
                    cloudinaryResponse.publicId(),
                    cloudinaryResponse.secureUrl(),
                    cloudinaryResponse.resourceType(),
                    cloudinaryResponse.format(),
                    cloudinaryResponse.bytes(),
                    cloudinaryResponse.width(),
                    cloudinaryResponse.height()
            );

            ProductoImagenCloudinary entity = productoImagenMapper.toEntity(
                    normalized,
                    producto,
                    sku,
                    cloudinaryResponse.assetId(),
                    cloudinaryResponse.publicId(),
                    cloudinaryResponse.version(),
                    cloudinaryResponse.secureUrl(),
                    cloudinaryResponse.url(),
                    cloudinaryResponse.resourceType(),
                    cloudinaryResponse.format(),
                    cloudinaryResponse.bytes(),
                    cloudinaryResponse.width(),
                    cloudinaryResponse.height(),
                    cloudinaryResponse.folder(),
                    cloudinaryResponse.originalFilename(),
                    actor.getIdUsuarioMs1()
            );
            entity.activar();

            ProductoImagenCloudinary saved = productoImagenRepository.saveAndFlush(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.IMAGEN_PRODUCTO_SUBIDA,
                    EntidadAuditada.PRODUCTO_IMAGEN_CLOUDINARY,
                    String.valueOf(saved.getIdImagen()),
                    "SUBIR_IMAGEN_PRODUCTO",
                    "Imagen de producto subida correctamente.",
                    auditMetadata(saved, actor, Map.of("cloudinaryPublicId", saved.getCloudinaryPublicId()))
            );

            registrarOutboxProducto(saved, ProductoEventType.IMAGEN_PRODUCTO_SNAPSHOT_ACTUALIZADO);

            return apiResponseFactory.dtoCreated(
                    "Imagen de producto subida correctamente.",
                    productoImagenMapper.toResponse(saved)
            );
        } catch (RuntimeException ex) {
            safeDeleteUploadedResource(cloudinaryResponse);
            throw ex;
        }
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoImagenResponseDto> actualizarMetadata(
            Long idImagen,
            ProductoImagenUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanUpdateMetadata(actor, employeeCanManageImages(actor));

        ProductoImagenCloudinary imagen = findActiveRequired(idImagen);
        ProductoImagenUpdateRequestDto normalized = normalizeUpdateRequest(request);

        productoImagenValidator.validateUpdate(
                normalized.altText(),
                normalized.titulo(),
                normalized.orden()
        );

        productoImagenMapper.updateEntity(imagen, normalized);

        ProductoImagenCloudinary saved = productoImagenRepository.saveAndFlush(imagen);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_IMAGEN_CLOUDINARY,
                String.valueOf(saved.getIdImagen()),
                "ACTUALIZAR_METADATA_IMAGEN_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of())
        );

        registrarOutboxProducto(saved, ProductoEventType.IMAGEN_PRODUCTO_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoImagenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoImagenResponseDto> marcarPrincipal(
            Long idImagen,
            ProductoImagenPrincipalRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanSetPrincipal(actor, employeeCanManageImages(actor));

        ProductoImagenCloudinary imagen = findActiveRequired(idImagen);
        ProductoImagenPrincipalRequestDto normalized = normalizePrincipalRequest(request, imagen);
        ProductoSku requestedSku = normalized.sku() == null ? null : resolveSkuRequired(normalized.sku());

        validatePrincipalScope(imagen, normalized, requestedSku);

        boolean principalSku = Boolean.TRUE.equals(normalized.principalSku());
        boolean principalProducto = Boolean.TRUE.equals(normalized.principalProducto());

        if (principalSku) {
            clearPrincipal(imagen.getProducto(), imagen.getSku());
        } else if (principalProducto) {
            clearPrincipal(imagen.getProducto(), null);
        }

        productoImagenMapper.markPrincipal(imagen, Boolean.TRUE);
        ProductoImagenCloudinary saved = productoImagenRepository.saveAndFlush(imagen);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_IMAGEN_CLOUDINARY,
                String.valueOf(saved.getIdImagen()),
                "MARCAR_IMAGEN_PRINCIPAL",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("principalSku", principalSku, "principalProducto", principalProducto))
        );

        registrarOutboxProducto(saved, ProductoEventType.IMAGEN_PRODUCTO_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoImagenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoImagenResponseDto> inactivar(
            Long idImagen,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanDeactivate(actor, employeeCanManageImages(actor));

        validateEstadoChangeRequest(request);

        ProductoImagenCloudinary imagen = findActiveRequired(idImagen);
        productoImagenMapper.deactivate(imagen);

        ProductoImagenCloudinary saved = productoImagenRepository.saveAndFlush(imagen);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.IMAGEN_PRODUCTO_INACTIVADA,
                EntidadAuditada.PRODUCTO_IMAGEN_CLOUDINARY,
                String.valueOf(saved.getIdImagen()),
                "INACTIVAR_IMAGEN_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarOutboxProducto(saved, ProductoEventType.IMAGEN_PRODUCTO_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoImagenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoImagenResponseDto> obtenerDetalle(Long idImagen) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        ProductoImagenCloudinary imagen = findActiveRequired(idImagen);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoImagenMapper.toResponse(imagen)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoImagenResponseDto>> listar(
            ProductoImagenFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest);
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<ProductoImagenResponseDto> response = paginationService.toPageResponseDto(
                productoImagenRepository.findAll(ProductoImagenSpecifications.fromFilter(filter), pageable),
                productoImagenMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoImagenResponseDto>> listarPorProducto(EntityReferenceDto productoReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        Producto producto = resolveProducto(productoReference);

        List<ProductoImagenResponseDto> response = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(productoImagenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoImagenResponseDto>> listarPorSku(EntityReferenceDto skuReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        ProductoSku sku = resolveSkuRequired(skuReference);

        List<ProductoImagenResponseDto> response = productoImagenRepository
                .findBySku_IdSkuAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(sku.getIdSku())
                .stream()
                .map(productoImagenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoImagenResponseDto> obtenerPrincipalProducto(EntityReferenceDto productoReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        Producto producto = resolveProducto(productoReference);

        ProductoImagenCloudinary imagen = productoImagenRepository
                .findFirstByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(
                        producto.getIdProducto()
                )
                .orElseThrow(() -> new NotFoundException(
                        "IMAGEN_PRINCIPAL_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoImagenMapper.toResponse(imagen)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoImagenResponseDto> obtenerPrincipalSku(EntityReferenceDto skuReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoImagenPolicy.ensureCanViewAdmin(actor);

        ProductoSku sku = resolveSkuRequired(skuReference);

        ProductoImagenCloudinary imagen = productoImagenRepository
                .findFirstBySku_IdSkuAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(sku.getIdSku())
                .orElseThrow(() -> new NotFoundException(
                        "IMAGEN_PRINCIPAL_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoImagenMapper.toResponse(imagen)
        );
    }

    private ProductoImagenCloudinary findActiveRequired(Long idImagen) {
        if (idImagen == null) {
            throw new ValidationException(
                    "IMAGEN_ID_REQUERIDO",
                    "Debe indicar la imagen solicitada."
            );
        }

        ProductoImagenCloudinary imagen = productoImagenRepository.findByIdImagenAndEstadoTrue(idImagen)
                .orElseThrow(() -> new NotFoundException(
                        "IMAGEN_PRODUCTO_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        productoImagenValidator.requireActive(imagen);
        return imagen;
    }

    private Producto resolveProducto(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "PRODUCTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el producto solicitado."
            );
        }

        return productoReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoProducto(), reference.codigo()),
                reference.slug(),
                reference.nombre()
        );
    }

    private ProductoSku resolveSkuRequired(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU solicitado."
            );
        }

        return productoSkuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );
    }

    private ProductoSku resolveOptionalSku(EntityReferenceDto reference) {
        if (reference == null) {
            return null;
        }

        return resolveSkuRequired(reference);
    }

    private ProductoImagenUploadRequestDto normalizeUploadRequest(ProductoImagenUploadRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "IMAGEN_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la imagen."
            );
        }

        return ProductoImagenUploadRequestDto.builder()
                .producto(request.producto())
                .sku(request.sku())
                .archivo(request.archivo())
                .altText(StringNormalizer.truncateOrNull(request.altText(), 250))
                .titulo(StringNormalizer.truncateOrNull(request.titulo(), 180))
                .orden(request.orden() == null ? 0 : request.orden())
                .principal(Boolean.TRUE.equals(request.principal()))
                .build();
    }

    private ProductoImagenUpdateRequestDto normalizeUpdateRequest(ProductoImagenUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "IMAGEN_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la imagen."
            );
        }

        return ProductoImagenUpdateRequestDto.builder()
                .altText(StringNormalizer.truncateOrNull(request.altText(), 250))
                .titulo(StringNormalizer.truncateOrNull(request.titulo(), 180))
                .orden(request.orden())
                .build();
    }

    private ProductoImagenPrincipalRequestDto normalizePrincipalRequest(
            ProductoImagenPrincipalRequestDto request,
            ProductoImagenCloudinary imagen
    ) {
        if (request == null) {
            return ProductoImagenPrincipalRequestDto.builder()
                    .principalProducto(imagen.getSku() == null)
                    .principalSku(imagen.getSku() != null)
                    .build();
        }

        return ProductoImagenPrincipalRequestDto.builder()
                .sku(request.sku())
                .principalProducto(Boolean.TRUE.equals(request.principalProducto()))
                .principalSku(Boolean.TRUE.equals(request.principalSku()))
                .build();
    }

    private void validatePrincipalScope(
            ProductoImagenCloudinary imagen,
            ProductoImagenPrincipalRequestDto request,
            ProductoSku requestedSku
    ) {
        boolean principalSku = Boolean.TRUE.equals(request.principalSku());
        boolean principalProducto = Boolean.TRUE.equals(request.principalProducto());

        if (principalSku == principalProducto) {
            throw new ValidationException(
                    "IMAGEN_PRINCIPAL_TIPO_REQUERIDO",
                    "Debe indicar si la imagen será principal del producto o del SKU."
            );
        }

        if (principalProducto && imagen.getSku() != null) {
            throw new ConflictException(
                    "IMAGEN_PRODUCTO_BASE_REQUERIDA",
                    "No se puede marcar como principal de producto una imagen asociada a un SKU."
            );
        }

        if (principalSku && imagen.getSku() == null) {
            throw new ConflictException(
                    "IMAGEN_SKU_REQUERIDA",
                    "No se puede marcar como principal de SKU una imagen que no pertenece a un SKU."
            );
        }

        if (requestedSku != null) {
            if (imagen.getSku() == null || !requestedSku.getIdSku().equals(imagen.getSku().getIdSku())) {
                throw new ConflictException(
                        "IMAGEN_SKU_NO_COINCIDE",
                        "El SKU indicado no coincide con el SKU asociado a la imagen."
                );
            }
        }
    }

    private void validateSkuBelongsToProduct(Producto producto, ProductoSku sku) {
        if (producto == null || sku == null) {
            return;
        }

        if (sku.getProducto() == null || !producto.getIdProducto().equals(sku.getProducto().getIdProducto())) {
            throw new ConflictException(
                    "SKU_NO_PERTENECE_PRODUCTO",
                    "El SKU no pertenece al producto indicado."
            );
        }
    }

    private void validateEstadoChangeRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null || Boolean.TRUE.equals(request.estado())) {
            throw new ValidationException(
                    "IMAGEN_ESTADO_INVALIDO",
                    "Debe indicar una operación válida de inactivación."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "IMAGEN_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private void clearPrincipal(Producto producto, ProductoSku sku) {
        if (producto == null) {
            return;
        }

        List<ProductoImagenCloudinary> anteriores = sku == null
                ? productoImagenRepository
                .findByProducto_IdProductoAndSkuIsNullAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
                        producto.getIdProducto()
                )
                : productoImagenRepository
                .findBySku_IdSkuAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(sku.getIdSku());

        List<ProductoImagenCloudinary> changed = anteriores.stream()
                .filter(item -> Boolean.TRUE.equals(item.getPrincipal()))
                .peek(item -> item.setPrincipal(Boolean.FALSE))
                .toList();

        if (!changed.isEmpty()) {
            productoImagenRepository.saveAll(changed);
        }
    }

    private Map<String, String> uploadMetadata(Producto producto, ProductoSku sku) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("id_producto", String.valueOf(producto.getIdProducto()));
        metadata.put("codigo_producto", producto.getCodigoProducto());

        if (sku != null) {
            metadata.put("id_sku", String.valueOf(sku.getIdSku()));
            metadata.put("codigo_sku", sku.getCodigoSku());
        }

        return metadata;
    }

    private List<String> uploadTags(Producto producto, ProductoSku sku) {
        if (sku == null) {
            return List.of("ms3", "producto", producto.getCodigoProducto());
        }

        return List.of("ms3", "producto", producto.getCodigoProducto(), "sku", sku.getCodigoSku());
    }

    private void registrarOutboxProducto(
            ProductoImagenCloudinary imagen,
            ProductoEventType eventType
    ) {
        Producto producto = imagen.getProducto();

        if (producto == null || producto.getIdProducto() == null) {
            return;
        }

        AuditContext context = AuditContextHolder.getOrEmpty();

        ProductoSnapshotEvent event = ProductoSnapshotEvent.of(
                eventType,
                producto.getIdProducto(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toProductoSnapshotPayload(producto),
                Map.of("source", "ProductoImagenService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private ProductoSnapshotPayload toProductoSnapshotPayload(Producto producto) {
        List<ProductoSkuSnapshotPayload> skus = productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(producto.getIdProducto())
                .stream()
                .map(this::toSkuPayload)
                .toList();

        List<ProductoImagenSnapshotPayload> imagenes = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(this::toImagenPayload)
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

    private ProductoSkuSnapshotPayload toSkuPayload(ProductoSku sku) {
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
                .atributos(List.of())
                .build();
    }

    private ProductoImagenSnapshotPayload toImagenPayload(ProductoImagenCloudinary imagen) {
        return ProductoImagenSnapshotPayload.builder()
                .idImagen(imagen.getIdImagen())
                .idProducto(imagen.getProducto() == null ? null : imagen.getProducto().getIdProducto())
                .idSku(imagen.getSku() == null ? null : imagen.getSku().getIdSku())
                .codigoSku(imagen.getSku() == null ? null : imagen.getSku().getCodigoSku())
                .cloudinaryAssetId(imagen.getCloudinaryAssetId())
                .cloudinaryPublicId(imagen.getCloudinaryPublicId())
                .cloudinaryVersion(imagen.getCloudinaryVersion())
                .secureUrl(imagen.getSecureUrl())
                .url(imagen.getUrl())
                .resourceType(imagen.getResourceType())
                .format(imagen.getFormat())
                .bytes(imagen.getBytes())
                .width(imagen.getWidth())
                .height(imagen.getHeight())
                .folder(imagen.getFolder())
                .originalFilename(imagen.getOriginalFilename())
                .altText(imagen.getAltText())
                .titulo(imagen.getTitulo())
                .orden(imagen.getOrden())
                .principal(imagen.getPrincipal())
                .estado(imagen.getEstado())
                .createdAt(imagen.getCreatedAt())
                .updatedAt(imagen.getUpdatedAt())
                .build();
    }

    private Map<String, Object> auditMetadata(
            ProductoImagenCloudinary imagen,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idImagen", imagen.getIdImagen());
        metadata.put("idProducto", imagen.getProducto() == null ? null : imagen.getProducto().getIdProducto());
        metadata.put("codigoProducto", imagen.getProducto() == null ? null : imagen.getProducto().getCodigoProducto());
        metadata.put("idSku", imagen.getSku() == null ? null : imagen.getSku().getIdSku());
        metadata.put("codigoSku", imagen.getSku() == null ? null : imagen.getSku().getCodigoSku());
        metadata.put("cloudinaryPublicId", imagen.getCloudinaryPublicId());
        metadata.put("cloudinaryAssetId", imagen.getCloudinaryAssetId());
        metadata.put("secureUrl", imagen.getSecureUrl());
        metadata.put("principal", imagen.getPrincipal());
        metadata.put("estado", imagen.getEstado());
        metadata.put("actor", actor == null ? null : actor.actorLabel());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private void safeDeleteUploadedResource(CloudinaryUploadResponse response) {
        if (response == null || !StringNormalizer.hasText(response.publicId())) {
            return;
        }

        try {
            cloudinaryService.eliminar(response.publicId(), response.resourceType(), Boolean.TRUE);
        } catch (RuntimeException cleanupException) {
            log.warn(
                    "No se pudo compensar la subida Cloudinary luego de fallo transaccional. publicId={}, resourceType={}",
                    response.publicId(),
                    response.resourceType(),
                    cleanupException
            );
        }
    }

    private boolean employeeCanManageImages(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeGestionarImagenes(actor.getIdUsuarioMs1());
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