// ruta: src/main/java/com/upsjb/ms3/controller/PromocionController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.promocion.filter.PromocionFilterDto;
import com.upsjb.ms3.dto.promocion.filter.PromocionSkuDescuentoFilterDto;
import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionDetailResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoCalculoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.PromocionService;
import com.upsjb.ms3.service.contract.PromocionSkuDescuentoService;
import com.upsjb.ms3.service.contract.PromocionVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/admin/promociones")
@Tag(
        name = "MS3 - Admin - Promociones",
        description = "Endpoints administrativos para gestionar promociones, versiones y descuentos por SKU."
)
public class PromocionController {

    private final PromocionService promocionService;
    private final PromocionVersionService promocionVersionService;
    private final PromocionSkuDescuentoService promocionSkuDescuentoService;

    @PostMapping
    @Operation(
            summary = "Crear promoción",
            description = "Registra una nueva campaña de promoción. La generación de código, autorización, auditoría y Outbox se ejecutan en el service."
    )
    public ResponseEntity<ApiResponseDto<PromocionResponseDto>> crear(
            @Valid @RequestBody PromocionCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(promocionService.crear(request));
    }

    @PutMapping("/{idPromocion}")
    @Operation(
            summary = "Actualizar promoción",
            description = "Actualiza datos base de una promoción. No modifica versiones, descuentos, ventas ni facturación."
    )
    public ResponseEntity<ApiResponseDto<PromocionResponseDto>> actualizar(
            @Parameter(description = "ID técnico de la promoción.", required = true)
            @Positive(message = "El ID de la promoción debe ser positivo.")
            @PathVariable Long idPromocion,
            @Valid @RequestBody PromocionUpdateRequestDto request
    ) {
        return ResponseEntity.ok(promocionService.actualizar(idPromocion, request));
    }

    @PatchMapping("/{idPromocion}/estado")
    @Operation(
            summary = "Activar o inactivar promoción",
            description = "Cambia el estado lógico de la promoción. No elimina registros físicos ni afecta ventas pasadas."
    )
    public ResponseEntity<ApiResponseDto<PromocionResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico de la promoción.", required = true)
            @Positive(message = "El ID de la promoción debe ser positivo.")
            @PathVariable Long idPromocion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(promocionService.inactivar(idPromocion, request));
    }

    @GetMapping("/{idPromocion}")
    @Operation(
            summary = "Obtener promoción por ID",
            description = "Obtiene la respuesta resumida de una promoción por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<PromocionResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico de la promoción.", required = true)
            @Positive(message = "El ID de la promoción debe ser positivo.")
            @PathVariable Long idPromocion
    ) {
        return ResponseEntity.ok(promocionService.obtenerPorId(idPromocion));
    }

    @GetMapping("/{idPromocion}/detalle")
    @Operation(
            summary = "Obtener detalle administrativo de promoción",
            description = "Obtiene el detalle administrativo de una promoción, incluyendo información preparada por el service."
    )
    public ResponseEntity<ApiResponseDto<PromocionDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico de la promoción.", required = true)
            @Positive(message = "El ID de la promoción debe ser positivo.")
            @PathVariable Long idPromocion
    ) {
        return ResponseEntity.ok(promocionService.obtenerDetalle(idPromocion));
    }

    @GetMapping
    @Operation(
            summary = "Listar promociones",
            description = "Lista promociones con filtros y paginación. La capa service/specification aplica criterios de búsqueda y estado."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute PromocionFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(promocionService.listar(filter, pageRequest));
    }

    @PostMapping("/versiones")
    @Operation(
            summary = "Crear versión de promoción",
            description = "Crea una nueva versión de promoción con vigencia y descuentos opcionales. Las fechas, estados y descuentos se validan en el service."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> crearVersion(
            @Valid @RequestBody PromocionVersionCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(promocionVersionService.crear(request));
    }

    @PatchMapping("/versiones/{idPromocionVersion}/estado")
    @Operation(
            summary = "Cambiar estado de versión de promoción",
            description = "Cambia el estado funcional de una versión de promoción. El service valida vigencia, transición y reglas de negocio."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> cambiarEstadoVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody PromocionVersionEstadoRequestDto request
    ) {
        return ResponseEntity.ok(promocionVersionService.cambiarEstado(idPromocionVersion, request));
    }

    @PatchMapping("/versiones/{idPromocionVersion}/activar")
    @Operation(
            summary = "Activar versión de promoción",
            description = "Activa una versión de promoción. La validación de fechas, vigencia única y auditoría se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> activarVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(promocionVersionService.activar(idPromocionVersion, request));
    }

    @PatchMapping("/versiones/{idPromocionVersion}/programar")
    @Operation(
            summary = "Programar versión de promoción",
            description = "Programa una versión de promoción para vigencia futura. El controller no calcula vigencias ni fechas efectivas."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> programarVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(promocionVersionService.programar(idPromocionVersion, request));
    }

    @PatchMapping("/versiones/{idPromocionVersion}/finalizar")
    @Operation(
            summary = "Finalizar versión de promoción",
            description = "Finaliza una versión de promoción sin modificar ventas pasadas ni facturación."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> finalizarVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(promocionVersionService.finalizar(idPromocionVersion, request));
    }

    @PatchMapping("/versiones/{idPromocionVersion}/cancelar")
    @Operation(
            summary = "Cancelar versión de promoción",
            description = "Cancela una versión de promoción. La auditoría y eventos Outbox se registran desde el service si corresponde."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> cancelarVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(promocionVersionService.cancelar(idPromocionVersion, request));
    }

    @GetMapping("/versiones/{idPromocionVersion}")
    @Operation(
            summary = "Obtener detalle de versión de promoción",
            description = "Obtiene el detalle de una versión de promoción por ID técnico."
    )
    public ResponseEntity<ApiResponseDto<PromocionVersionResponseDto>> obtenerVersionDetalle(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion
    ) {
        return ResponseEntity.ok(promocionVersionService.obtenerDetalle(idPromocionVersion));
    }

    @GetMapping("/versiones")
    @Operation(
            summary = "Listar versiones de promoción",
            description = "Lista versiones de promoción con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>>> listarVersiones(
            @Valid @ParameterObject @ModelAttribute PromocionVersionFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(promocionVersionService.listar(filter, pageRequest));
    }

    @GetMapping("/versiones/por-promocion")
    @Operation(
            summary = "Listar versiones por promoción",
            description = "Lista versiones usando referencia funcional de promoción: id, código, nombre, slug o códigoPromocion."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>>> listarVersionesPorPromocion(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto promocion,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(promocionVersionService.listarPorPromocion(promocion, pageRequest));
    }

    @PostMapping("/versiones/{idPromocionVersion}/descuentos")
    @Operation(
            summary = "Agregar descuento por SKU a versión de promoción",
            description = "Asocia un SKU con descuento a una versión de promoción. El service valida SKU, precio, duplicados, margen y reglas de descuento."
    )
    public ResponseEntity<ApiResponseDto<PromocionSkuDescuentoResponseDto>> agregarDescuentoSku(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody PromocionSkuDescuentoCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(promocionSkuDescuentoService.agregar(idPromocionVersion, request));
    }

    @PostMapping("/versiones/{idPromocionVersion}/descuentos/calcular")
    @Operation(
            summary = "Calcular descuento estimado para SKU",
            description = "Calcula el resultado estimado de un descuento antes de persistirlo. No modifica precio base, promoción ni ventas."
    )
    public ResponseEntity<ApiResponseDto<PromocionSkuDescuentoCalculoResponseDto>> calcularDescuentoSku(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @RequestBody PromocionSkuDescuentoCreateRequestDto request
    ) {
        return ResponseEntity.ok(promocionSkuDescuentoService.calcular(idPromocionVersion, request));
    }

    @PutMapping("/descuentos/{idPromocionSkuDescuentoVersion}")
    @Operation(
            summary = "Actualizar descuento de SKU",
            description = "Actualiza un descuento por SKU de una versión de promoción. El service valida que la modificación sea funcionalmente permitida."
    )
    public ResponseEntity<ApiResponseDto<PromocionSkuDescuentoResponseDto>> actualizarDescuentoSku(
            @Parameter(description = "ID técnico del descuento de SKU versionado.", required = true)
            @Positive(message = "El ID del descuento de SKU debe ser positivo.")
            @PathVariable Long idPromocionSkuDescuentoVersion,
            @Valid @RequestBody PromocionSkuDescuentoUpdateRequestDto request
    ) {
        return ResponseEntity.ok(
                promocionSkuDescuentoService.actualizar(idPromocionSkuDescuentoVersion, request)
        );
    }

    @PatchMapping("/descuentos/{idPromocionSkuDescuentoVersion}/estado")
    @Operation(
            summary = "Activar o inactivar descuento de SKU",
            description = "Cambia el estado lógico de un descuento de SKU. No elimina historial físico."
    )
    public ResponseEntity<ApiResponseDto<PromocionSkuDescuentoResponseDto>> cambiarEstadoDescuentoSku(
            @Parameter(description = "ID técnico del descuento de SKU versionado.", required = true)
            @Positive(message = "El ID del descuento de SKU debe ser positivo.")
            @PathVariable Long idPromocionSkuDescuentoVersion,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(
                promocionSkuDescuentoService.inactivar(idPromocionSkuDescuentoVersion, request)
        );
    }

    @GetMapping("/descuentos/{idPromocionSkuDescuentoVersion}")
    @Operation(
            summary = "Obtener detalle de descuento de SKU",
            description = "Obtiene el detalle de un descuento por SKU versionado."
    )
    public ResponseEntity<ApiResponseDto<PromocionSkuDescuentoResponseDto>> obtenerDetalleDescuentoSku(
            @Parameter(description = "ID técnico del descuento de SKU versionado.", required = true)
            @Positive(message = "El ID del descuento de SKU debe ser positivo.")
            @PathVariable Long idPromocionSkuDescuentoVersion
    ) {
        return ResponseEntity.ok(
                promocionSkuDescuentoService.obtenerDetalle(idPromocionSkuDescuentoVersion)
        );
    }

    @GetMapping("/descuentos")
    @Operation(
            summary = "Listar descuentos de SKU",
            description = "Lista descuentos de SKU con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>>> listarDescuentos(
            @Valid @ParameterObject @ModelAttribute PromocionSkuDescuentoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(promocionSkuDescuentoService.listar(filter, pageRequest));
    }

    @GetMapping("/versiones/{idPromocionVersion}/descuentos")
    @Operation(
            summary = "Listar descuentos por versión de promoción",
            description = "Lista descuentos asociados a una versión de promoción."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>>> listarDescuentosPorVersion(
            @Parameter(description = "ID técnico de la versión de promoción.", required = true)
            @Positive(message = "El ID de la versión de promoción debe ser positivo.")
            @PathVariable Long idPromocionVersion,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(
                promocionSkuDescuentoService.listarPorVersion(idPromocionVersion, pageRequest)
        );
    }

    @GetMapping("/descuentos/por-sku")
    @Operation(
            summary = "Listar descuentos por SKU",
            description = "Lista descuentos de promoción usando referencia funcional de SKU: id, código, codigoSku o barcode."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>>> listarDescuentosPorSku(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto sku,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(promocionSkuDescuentoService.listarPorSku(sku, pageRequest));
    }

    @GetMapping("/descuentos/aplicables/por-sku")
    @Operation(
            summary = "Listar descuentos aplicables por SKU",
            description = "Lista descuentos aplicables para un SKU usando referencia funcional. La vigencia y aplicabilidad se calculan en el service."
    )
    public ResponseEntity<ApiResponseDto<List<PromocionSkuDescuentoResponseDto>>> listarDescuentosAplicablesPorSku(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto sku
    ) {
        return ResponseEntity.ok(promocionSkuDescuentoService.listarAplicablesPorSku(sku));
    }
}