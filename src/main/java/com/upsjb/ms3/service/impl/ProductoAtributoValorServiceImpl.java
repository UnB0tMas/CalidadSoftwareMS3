// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoAtributoValorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.mapper.ProductoAtributoValorMapper;
import com.upsjb.ms3.policy.ProductoPolicy;
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
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.reference.AtributoReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.AtributoValidator;
import com.upsjb.ms3.validator.ProductoValidator;
import com.upsjb.ms3.validator.TipoProductoAtributoValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoAtributoValorServiceImpl implements ProductoAtributoValorService {

    private final ProductoRepository productoRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final ProductoAtributoValorMapper productoAtributoValorMapper;
    private final AtributoReferenceResolver atributoReferenceResolver;
    private final ProductoValidator productoValidator;
    private final AtributoValidator atributoValidator;
    private final TipoProductoAtributoValidator tipoProductoAtributoValidator;
    private final ProductoPolicy productoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            Long idProducto,
            ProductoAtributoValorRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdate(actor, employeeCanUpdateAttributes(actor));

        Producto producto = findProductoRequired(idProducto);
        ProductoAtributoValorRequestDto normalized = normalizeRequest(request);
        Atributo atributo = resolveAtributo(normalized.atributo());

        TipoProductoAtributo relation = tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        producto.getTipoProducto().getIdTipoProducto(),
                        atributo.getIdAtributo()
                )
                .orElseThrow(() -> new ConflictException(
                        "ATRIBUTO_NO_ASOCIADO_TIPO_PRODUCTO",
                        "El atributo no está asociado al tipo de producto."
                ));

        tipoProductoAtributoValidator.requireActive(relation);
        atributoValidator.validateValueByType(
                atributo,
                normalized.valorTexto(),
                normalized.valorNumero(),
                normalized.valorBoolean(),
                normalized.valorFecha()
        );

        ProductoAtributoValor entity = productoAtributoValorRepository
                .findByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(
                        producto.getIdProducto(),
                        atributo.getIdAtributo()
                )
                .orElse(null);

        boolean created = entity == null;

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
                "Operación realizada correctamente.",
                auditMetadata(saved)
        );

        registrarOutboxProductoActualizado(producto);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoAtributoValorMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            Long idProducto,
            List<ProductoAtributoValorRequestDto> request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanUpdate(actor, employeeCanUpdateAttributes(actor));

        Producto producto = findProductoRequired(idProducto);

        productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .forEach(valor -> {
                    valor.inactivar();
                    productoAtributoValorRepository.save(valor);
                });

        List<ProductoAtributoValorResponseDto> response = (request == null ? List.<ProductoAtributoValorRequestDto>of() : request)
                .stream()
                .map(item -> guardarValorInterno(producto, item))
                .map(productoAtributoValorMapper::toResponse)
                .toList();

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO,
                String.valueOf(producto.getIdProducto()),
                "REEMPLAZAR_ATRIBUTOS_PRODUCTO",
                "Operación realizada correctamente.",
                Map.of("idProducto", producto.getIdProducto(), "cantidadAtributos", response.size())
        );

        registrarOutboxProductoActualizado(producto);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(Long idProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        Producto producto = findProductoRequired(idProducto);

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
    public ApiResponseDto<ProductoAtributoValorResponseDto> obtenerDetalle(Long idProductoAtributoValor) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoPolicy.ensureCanViewAdmin(actor);

        ProductoAtributoValor entity = findValorRequired(idProductoAtributoValor);

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
        productoPolicy.ensureCanUpdate(actor, employeeCanUpdateAttributes(actor));

        validateEstadoChangeRequest(request);

        ProductoAtributoValor entity = findValorRequired(idProductoAtributoValor);
        entity.inactivar();

        ProductoAtributoValor saved = productoAtributoValorRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PRODUCTO_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_ATRIBUTO_VALOR,
                String.valueOf(saved.getIdProductoAtributoValor()),
                "INACTIVAR_VALOR_ATRIBUTO_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved)
        );

        registrarOutboxProductoActualizado(saved.getProducto());

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                productoAtributoValorMapper.toResponse(saved)
        );
    }

    private ProductoAtributoValor guardarValorInterno(
            Producto producto,
            ProductoAtributoValorRequestDto request
    ) {
        ProductoAtributoValorRequestDto normalized = normalizeRequest(request);
        Atributo atributo = resolveAtributo(normalized.atributo());

        TipoProductoAtributo relation = tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        producto.getTipoProducto().getIdTipoProducto(),
                        atributo.getIdAtributo()
                )
                .orElseThrow(() -> new ConflictException(
                        "ATRIBUTO_NO_ASOCIADO_TIPO_PRODUCTO",
                        "El atributo no está asociado al tipo de producto."
                ));

        tipoProductoAtributoValidator.requireActive(relation);
        atributoValidator.validateValueByType(
                atributo,
                normalized.valorTexto(),
                normalized.valorNumero(),
                normalized.valorBoolean(),
                normalized.valorFecha()
        );

        ProductoAtributoValor entity = productoAtributoValorMapper.toEntity(normalized, producto, atributo);
        entity.activar();

        return productoAtributoValorRepository.saveAndFlush(entity);
    }

    private Producto findProductoRequired(Long idProducto) {
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

    private ProductoAtributoValor findValorRequired(Long idProductoAtributoValor) {
        if (idProductoAtributoValor == null) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_ID_REQUERIDO",
                    "Debe indicar el valor de atributo solicitado."
            );
        }

        return productoAtributoValorRepository
                .findByIdProductoAtributoValorAndEstadoTrue(idProductoAtributoValor)
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
                .valorTexto(StringNormalizer.truncateOrNull(request.valorTexto(), 500))
                .valorNumero(request.valorNumero())
                .valorBoolean(request.valorBoolean())
                .valorFecha(request.valorFecha())
                .build();
    }

    private Atributo resolveAtributo(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ATRIBUTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el atributo."
            );
        }

        return atributoReferenceResolver.resolve(
                reference.id(),
                reference.codigo(),
                reference.nombre()
        );
    }

    private void validateEstadoChangeRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null || Boolean.TRUE.equals(request.estado())) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_ESTADO_INVALIDO",
                    "Debe indicar una operación válida de inactivación."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "PRODUCTO_ATRIBUTO_VALOR_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
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
                ProductoSnapshotPayload.builder()
                        .idProducto(producto.getIdProducto())
                        .codigoProducto(producto.getCodigoProducto())
                        .nombre(producto.getNombre())
                        .slug(producto.getSlug())
                        .estadoRegistro(producto.getEstadoRegistro() == null ? null : producto.getEstadoRegistro().getCode())
                        .estadoPublicacion(producto.getEstadoPublicacion() == null ? null : producto.getEstadoPublicacion().getCode())
                        .estadoVenta(producto.getEstadoVenta() == null ? null : producto.getEstadoVenta().getCode())
                        .visiblePublico(producto.getVisiblePublico())
                        .vendible(producto.getVendible())
                        .estado(producto.getEstado())
                        .createdAt(producto.getCreatedAt())
                        .updatedAt(producto.getUpdatedAt())
                        .build(),
                Map.of("source", "ProductoAtributoValorService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private Map<String, Object> auditMetadata(ProductoAtributoValor entity) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idProductoAtributoValor", entity.getIdProductoAtributoValor());
        metadata.put("idProducto", entity.getProducto() == null ? null : entity.getProducto().getIdProducto());
        metadata.put("codigoProducto", entity.getProducto() == null ? null : entity.getProducto().getCodigoProducto());
        metadata.put("idAtributo", entity.getAtributo() == null ? null : entity.getAtributo().getIdAtributo());
        metadata.put("codigoAtributo", entity.getAtributo() == null ? null : entity.getAtributo().getCodigo());
        metadata.put("valorTexto", entity.getValorTexto());
        metadata.put("valorNumero", entity.getValorNumero());
        metadata.put("valorBoolean", entity.getValorBoolean());
        metadata.put("valorFecha", entity.getValorFecha());
        metadata.put("estado", entity.getEstado());
        return metadata;
    }

    private boolean employeeCanUpdateAttributes(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeActualizarAtributos(actor.getIdUsuarioMs1());
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }
}