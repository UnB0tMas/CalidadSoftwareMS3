// ruta: src/main/java/com/upsjb/ms3/service/impl/InventarioOperacionServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.SalidaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import com.upsjb.ms3.dto.inventario.operacion.request.AjusteInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.EntradaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.SalidaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.TransferenciaInventarioLineaRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.TransferenciaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.response.MovimientoInventarioLoteResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.service.contract.InventarioOperacionService;
import com.upsjb.ms3.service.contract.MovimientoInventarioService;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.reference.AlmacenReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioOperacionServiceImpl implements InventarioOperacionService {

    private static final int MAX_LINEAS = 200;
    private static final String REFERENCIA_TIPO_LOTE = "OPERACION_INVENTARIO";
    private static final String REFERENCIA_TIPO_TRANSFERENCIA = "TRANSFERENCIA_INVENTARIO";

    private final MovimientoInventarioService movimientoInventarioService;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final AlmacenReferenceResolver almacenReferenceResolver;
    private final StockSkuRepository stockSkuRepository;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarEntradas(
            EntradaInventarioLoteRequestDto request
    ) {
        List<EntradaInventarioRequestDto> lineas = requireLines(
                request == null ? null : request.lineas(),
                "ENTRADAS_LOTE_REQUERIDAS",
                "Debe agregar al menos un producto al ingreso."
        );
        ensureUniqueMovementTargets(lineas.stream()
                .map(linea -> new TargetReference(linea.sku(), linea.almacen()))
                .toList());

        String codigoOperacion = operationCode("ENT");
        List<MovimientoInventarioResponseDto> movimientos = new ArrayList<>(lineas.size());

        for (EntradaInventarioRequestDto linea : lineas) {
            EntradaInventarioRequestDto normalized = EntradaInventarioRequestDto.builder()
                    .sku(linea.sku())
                    .almacen(linea.almacen())
                    .tipoMovimiento(linea.tipoMovimiento())
                    .motivoMovimiento(linea.motivoMovimiento())
                    .cantidad(linea.cantidad())
                    .costoUnitario(linea.costoUnitario())
                    .referenciaTipo(firstText(linea.referenciaTipo(), REFERENCIA_TIPO_LOTE))
                    .referenciaIdExterno(firstText(linea.referenciaIdExterno(), codigoOperacion))
                    .observacion(linea.observacion())
                    .build();

            movimientos.add(requireMovement(
                    movimientoInventarioService.registrarEntrada(normalized),
                    "No se pudo obtener el movimiento generado para una entrada."
            ));
        }

        return created(codigoOperacion, "ENTRADA_LOTE", lineas.size(), movimientos);
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarSalidas(
            SalidaInventarioLoteRequestDto request
    ) {
        List<SalidaInventarioRequestDto> lineas = requireLines(
                request == null ? null : request.lineas(),
                "SALIDAS_LOTE_REQUERIDAS",
                "Debe agregar al menos un producto a la salida."
        );
        ensureUniqueMovementTargets(lineas.stream()
                .map(linea -> new TargetReference(linea.sku(), linea.almacen()))
                .toList());

        String codigoOperacion = operationCode("SAL");
        List<MovimientoInventarioResponseDto> movimientos = new ArrayList<>(lineas.size());

        for (SalidaInventarioRequestDto linea : lineas) {
            SalidaInventarioRequestDto normalized = SalidaInventarioRequestDto.builder()
                    .sku(linea.sku())
                    .almacen(linea.almacen())
                    .tipoMovimiento(linea.tipoMovimiento())
                    .motivoMovimiento(linea.motivoMovimiento())
                    .cantidad(linea.cantidad())
                    .referenciaTipo(firstText(linea.referenciaTipo(), REFERENCIA_TIPO_LOTE))
                    .referenciaIdExterno(firstText(linea.referenciaIdExterno(), codigoOperacion))
                    .observacion(linea.observacion())
                    .build();

            movimientos.add(requireMovement(
                    movimientoInventarioService.registrarSalida(normalized),
                    "No se pudo obtener el movimiento generado para una salida."
            ));
        }

        return created(codigoOperacion, "SALIDA_LOTE", lineas.size(), movimientos);
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarAjustes(
            AjusteInventarioLoteRequestDto request
    ) {
        List<AjusteInventarioRequestDto> lineas = requireLines(
                request == null ? null : request.lineas(),
                "AJUSTES_LOTE_REQUERIDOS",
                "Debe agregar al menos un producto al ajuste."
        );
        ensureUniqueMovementTargets(lineas.stream()
                .map(linea -> new TargetReference(linea.sku(), linea.almacen()))
                .toList());

        String codigoOperacion = operationCode("AJU");
        List<MovimientoInventarioResponseDto> movimientos = new ArrayList<>(lineas.size());

        for (AjusteInventarioRequestDto linea : lineas) {
            AjusteInventarioRequestDto normalized = AjusteInventarioRequestDto.builder()
                    .sku(linea.sku())
                    .almacen(linea.almacen())
                    .tipoMovimiento(linea.tipoMovimiento())
                    .motivoMovimiento(linea.motivoMovimiento())
                    .cantidad(linea.cantidad())
                    .costoUnitario(linea.costoUnitario())
                    .stockFisicoEsperado(linea.stockFisicoEsperado())
                    .referenciaIdExterno(firstText(linea.referenciaIdExterno(), codigoOperacion))
                    .observacion(linea.observacion())
                    .build();

            movimientos.add(requireMovement(
                    movimientoInventarioService.registrarAjuste(normalized),
                    "No se pudo obtener el movimiento generado para un ajuste."
            ));
        }

        return created(codigoOperacion, "AJUSTE_LOTE", lineas.size(), movimientos);
    }

    @Override
    @Transactional
    public ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarTransferencia(
            TransferenciaInventarioRequestDto request
    ) {
        if (request == null) {
            throw new ValidationException(
                    "TRANSFERENCIA_REQUEST_REQUERIDA",
                    "Debe enviar los datos de la transferencia."
            );
        }

        List<TransferenciaInventarioLineaRequestDto> lineas = requireLines(
                request.lineas(),
                "TRANSFERENCIA_LINEAS_REQUERIDAS",
                "Debe agregar al menos un producto a la transferencia."
        );

        Almacen origen = almacenReferenceResolver.resolve(
                referenceId(request.almacenOrigen()),
                referenceWarehouseCode(request.almacenOrigen()),
                referenceName(request.almacenOrigen())
        );
        Almacen destino = almacenReferenceResolver.resolve(
                referenceId(request.almacenDestino()),
                referenceWarehouseCode(request.almacenDestino()),
                referenceName(request.almacenDestino())
        );

        if (origen.getIdAlmacen().equals(destino.getIdAlmacen())) {
            throw new ConflictException(
                    "TRANSFERENCIA_ALMACENES_IGUALES",
                    "El almacén de origen y el almacén de destino deben ser diferentes."
            );
        }

        String codigoOperacion = firstText(
                request.referenciaIdExterno(),
                operationCode("TRF")
        );
        String observacion = StringNormalizer.truncateOrNull(request.observacion(), 500);
        List<MovimientoInventarioResponseDto> movimientos = new ArrayList<>(lineas.size() * 2);
        Set<Long> skuIds = new HashSet<>();

        for (TransferenciaInventarioLineaRequestDto linea : lineas) {
            ProductoSku sku = resolveSku(linea.sku());

            if (!skuIds.add(sku.getIdSku())) {
                throw new ConflictException(
                        "TRANSFERENCIA_SKU_DUPLICADO",
                        "Un producto no puede aparecer más de una vez en la misma transferencia."
                );
            }

            StockSku stockOrigen = stockSkuRepository
                    .findActivoBySkuAndAlmacenForUpdate(sku.getIdSku(), origen.getIdAlmacen())
                    .orElseThrow(() -> new NotFoundException(
                            "STOCK_ORIGEN_NO_ENCONTRADO",
                            "No se encontró stock activo del producto en el almacén de origen."
                    ));

            BigDecimal costoTransferencia = firstNonNull(
                    stockOrigen.getCostoPromedioActual(),
                    stockOrigen.getUltimoCostoCompra()
            );

            EntityReferenceDto skuRef = idReference(sku.getIdSku());
            EntityReferenceDto origenRef = idReference(origen.getIdAlmacen());
            EntityReferenceDto destinoRef = idReference(destino.getIdAlmacen());

            SalidaInventarioRequestDto salida = SalidaInventarioRequestDto.builder()
                    .sku(skuRef)
                    .almacen(origenRef)
                    .tipoMovimiento(TipoMovimientoInventario.SALIDA_TRASLADO)
                    .motivoMovimiento(MotivoMovimientoInventario.TRASLADO)
                    .cantidad(linea.cantidad())
                    .referenciaTipo(REFERENCIA_TIPO_TRANSFERENCIA)
                    .referenciaIdExterno(codigoOperacion)
                    .observacion(observacion)
                    .build();

            EntradaInventarioRequestDto entrada = EntradaInventarioRequestDto.builder()
                    .sku(skuRef)
                    .almacen(destinoRef)
                    .tipoMovimiento(TipoMovimientoInventario.ENTRADA_TRASLADO)
                    .motivoMovimiento(MotivoMovimientoInventario.TRASLADO)
                    .cantidad(linea.cantidad())
                    .costoUnitario(costoTransferencia)
                    .referenciaTipo(REFERENCIA_TIPO_TRANSFERENCIA)
                    .referenciaIdExterno(codigoOperacion)
                    .observacion(observacion)
                    .build();

            movimientos.add(requireMovement(
                    movimientoInventarioService.registrarSalida(salida),
                    "No se pudo obtener el movimiento de salida de la transferencia."
            ));
            movimientos.add(requireMovement(
                    movimientoInventarioService.registrarEntrada(entrada),
                    "No se pudo obtener el movimiento de entrada de la transferencia."
            ));
        }

        return created(codigoOperacion, "TRANSFERENCIA", lineas.size(), movimientos);
    }

    private <T> List<T> requireLines(List<T> lineas, String code, String message) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ValidationException(code, message);
        }
        if (lineas.size() > MAX_LINEAS) {
            throw new ValidationException(
                    "OPERACION_LOTE_LIMITE_EXCEDIDO",
                    "No puede procesar más de " + MAX_LINEAS + " productos por operación."
            );
        }
        return lineas;
    }

    private void ensureUniqueMovementTargets(List<TargetReference> targets) {
        Set<String> uniqueTargets = new HashSet<>();

        for (TargetReference target : targets) {
            ProductoSku sku = resolveSku(target.sku());
            Almacen almacen = resolveWarehouse(target.almacen());
            String key = sku.getIdSku() + ":" + almacen.getIdAlmacen();

            if (!uniqueTargets.add(key)) {
                throw new ConflictException(
                        "OPERACION_PRODUCTO_ALMACEN_DUPLICADO",
                        "Un mismo producto y almacén no puede repetirse dentro de la operación."
                );
            }
        }
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException("SKU_REFERENCIA_REQUERIDA", "Debe indicar el SKU solicitado.");
        }
        return productoSkuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );
    }

    private Almacen resolveWarehouse(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException("ALMACEN_REFERENCIA_REQUERIDA", "Debe indicar el almacén solicitado.");
        }
        return almacenReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoAlmacen(), reference.codigo()),
                reference.nombre()
        );
    }

    private ApiResponseDto<MovimientoInventarioLoteResponseDto> created(
            String codigoOperacion,
            String tipoOperacion,
            int totalLineas,
            List<MovimientoInventarioResponseDto> movimientos
    ) {
        MovimientoInventarioLoteResponseDto response = MovimientoInventarioLoteResponseDto.builder()
                .codigoOperacion(codigoOperacion)
                .tipoOperacion(tipoOperacion)
                .totalLineas(totalLineas)
                .totalMovimientos(movimientos.size())
                .movimientos(List.copyOf(movimientos))
                .build();

        return apiResponseFactory.dtoCreated(
                "Operación de inventario registrada correctamente.",
                response
        );
    }

    private MovimientoInventarioResponseDto requireMovement(
            ApiResponseDto<MovimientoInventarioResponseDto> response,
            String message
    ) {
        if (response == null || response.data() == null) {
            throw new ConflictException("MOVIMIENTO_RESULTADO_INVALIDO", message);
        }
        return response.data();
    }

    private String operationCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private EntityReferenceDto idReference(Long id) {
        return EntityReferenceDto.builder().id(id).build();
    }

    private Long referenceId(EntityReferenceDto reference) {
        return reference == null ? null : reference.id();
    }

    private String referenceWarehouseCode(EntityReferenceDto reference) {
        if (reference == null) {
            return null;
        }
        return firstText(reference.codigoAlmacen(), reference.codigo());
    }

    private String referenceName(EntityReferenceDto reference) {
        return reference == null ? null : reference.nombre();
    }

    private String firstText(String first, String second) {
        String cleanFirst = StringNormalizer.cleanOrNull(first);
        return cleanFirst == null ? StringNormalizer.cleanOrNull(second) : cleanFirst;
    }

    private BigDecimal firstNonNull(BigDecimal first, BigDecimal second) {
        return first == null ? second : first;
    }

    private record TargetReference(EntityReferenceDto sku, EntityReferenceDto almacen) {
    }
}
