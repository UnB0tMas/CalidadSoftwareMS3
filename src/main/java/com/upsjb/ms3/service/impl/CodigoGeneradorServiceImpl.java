// ruta: src/main/java/com/upsjb/ms3/service/impl/CodigoGeneradorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.CorrelativoCodigo;
import com.upsjb.ms3.domain.value.CodigoGeneradoValue;
import com.upsjb.ms3.repository.CorrelativoCodigoRepository;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.shared.code.CodigoGenerator;
import com.upsjb.ms3.shared.code.CodigoSequenceLock;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodigoGeneradorServiceImpl implements CodigoGeneradorService {

    private static final int DEFAULT_LENGTH = 6;
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 12;

    private static final String ENTIDAD_PRODUCTO = "PRODUCTO";
    private static final String ENTIDAD_SKU = "SKU";
    private static final String ENTIDAD_PROMOCION = "PROMOCION";
    private static final String ENTIDAD_COMPRA = "COMPRA";
    private static final String ENTIDAD_RESERVA_STOCK = "RESERVA_STOCK";
    private static final String ENTIDAD_MOVIMIENTO_INVENTARIO = "MOVIMIENTO_INVENTARIO";

    private static final Map<String, CodigoDefault> DEFAULTS = Map.ofEntries(
            Map.entry(ENTIDAD_PRODUCTO, new CodigoDefault("PROD", DEFAULT_LENGTH, "Código de producto")),
            Map.entry(ENTIDAD_SKU, new CodigoDefault("SKU", DEFAULT_LENGTH, "Código de SKU")),
            Map.entry(ENTIDAD_PROMOCION, new CodigoDefault("PROM", DEFAULT_LENGTH, "Código de promoción")),
            Map.entry(ENTIDAD_COMPRA, new CodigoDefault("COMP", DEFAULT_LENGTH, "Código de compra")),
            Map.entry(ENTIDAD_RESERVA_STOCK, new CodigoDefault("RES", DEFAULT_LENGTH, "Código de reserva de stock")),
            Map.entry(
                    ENTIDAD_MOVIMIENTO_INVENTARIO,
                    new CodigoDefault("MOV", DEFAULT_LENGTH, "Código de movimiento de inventario")
            ),
            Map.entry("ALMACEN", new CodigoDefault("ALM", DEFAULT_LENGTH, "Código de almacén")),
            Map.entry("PROVEEDOR", new CodigoDefault("PROV", DEFAULT_LENGTH, "Código de proveedor")),
            Map.entry("CATEGORIA", new CodigoDefault("CAT", DEFAULT_LENGTH, "Código de categoría")),
            Map.entry("MARCA", new CodigoDefault("MAR", DEFAULT_LENGTH, "Código de marca")),
            Map.entry("ATRIBUTO", new CodigoDefault("ATR", DEFAULT_LENGTH, "Código de atributo")),
            Map.entry("TIPO_PRODUCTO", new CodigoDefault("TPROD", DEFAULT_LENGTH, "Código de tipo de producto"))
    );

    private final CorrelativoCodigoRepository correlativoCodigoRepository;
    private final CodigoGenerator codigoGenerator;
    private final CodigoSequenceLock codigoSequenceLock;

    @Override
    @Transactional
    public String generarCodigoProducto() {
        return generarCodigo(ENTIDAD_PRODUCTO);
    }

    @Override
    @Transactional
    public String generarCodigoSku() {
        return generarCodigo(ENTIDAD_SKU);
    }

    @Override
    @Transactional
    public String generarCodigoPromocion() {
        return generarCodigo(ENTIDAD_PROMOCION);
    }

    @Override
    @Transactional
    public String generarCodigoCompra() {
        return generarCodigo(ENTIDAD_COMPRA);
    }

    @Override
    @Transactional
    public String generarCodigoReservaStock() {
        return generarCodigo(ENTIDAD_RESERVA_STOCK);
    }

    @Override
    @Transactional
    public String generarCodigoMovimientoInventario() {
        return generarCodigo(ENTIDAD_MOVIMIENTO_INVENTARIO);
    }

    @Override
    @Transactional
    public String generarCodigo(String entidad) {
        String safeEntidad = normalizeEntidad(entidad);
        CodigoDefault defaults = DEFAULTS.getOrDefault(
                safeEntidad,
                new CodigoDefault(defaultPrefixFromEntidad(safeEntidad), DEFAULT_LENGTH, "Código generado de " + safeEntidad)
        );

        return generarCodigo(safeEntidad, defaults.prefijo(), defaults.longitud());
    }

    @Override
    @Transactional
    public String generarCodigo(String entidad, String prefijoPorDefecto, int longitudPorDefecto) {
        String safeEntidad = normalizeEntidad(entidad);
        String safePrefix = normalizePrefix(prefijoPorDefecto);
        int safeLength = normalizeLength(longitudPorDefecto);

        try (CodigoSequenceLock.LockHandle ignored = codigoSequenceLock.lock(safeEntidad)) {
            CorrelativoCodigo correlativo = correlativoCodigoRepository
                    .findActivoByEntidadForUpdate(safeEntidad)
                    .orElseGet(() -> createCorrelativo(safeEntidad, safePrefix, safeLength));

            validateCorrelativo(correlativo);

            Long nextNumber = correlativo.siguienteNumero();
            String generated = codigoGenerator.format(
                    correlativo.getPrefijo(),
                    normalizeLength(correlativo.getLongitud()),
                    nextNumber
            );

            String validated = CodigoGeneradoValue.of(generated).raw();

            correlativoCodigoRepository.saveAndFlush(correlativo);

            log.debug(
                    "Código generado. entidad={}, prefijo={}, numero={}, codigo={}",
                    safeEntidad,
                    correlativo.getPrefijo(),
                    nextNumber,
                    validated
            );

            return validated;
        }
    }

    private CorrelativoCodigo createCorrelativo(String entidad, String prefijo, int longitud) {
        CorrelativoCodigo correlativo = new CorrelativoCodigo();
        correlativo.setEntidad(entidad);
        correlativo.setPrefijo(prefijo);
        correlativo.setLongitud(longitud);
        correlativo.setUltimoNumero(0L);
        correlativo.setDescripcion(resolveDescription(entidad));
        correlativo.activar();

        return correlativo;
    }

    private void validateCorrelativo(CorrelativoCodigo correlativo) {
        if (correlativo == null) {
            throw new ConflictException(
                    "CORRELATIVO_NO_CONFIGURADO",
                    "No se pudo resolver el correlativo de código."
            );
        }

        if (!correlativo.isActivo()) {
            throw new ConflictException(
                    "CORRELATIVO_INACTIVO",
                    "No se puede generar código porque el correlativo está inactivo."
            );
        }

        normalizeEntidad(correlativo.getEntidad());
        normalizePrefix(correlativo.getPrefijo());
        normalizeLength(correlativo.getLongitud());
    }

    private String normalizeEntidad(String entidad) {
        if (!StringNormalizer.hasText(entidad)) {
            throw new ValidationException(
                    "ENTIDAD_CORRELATIVO_REQUERIDA",
                    "Debe indicar la entidad para generar el código."
            );
        }

        String normalized = StringNormalizer
                .upperWithoutAccents(entidad)
                .replaceAll("[^A-Z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        if (!StringNormalizer.hasText(normalized)) {
            throw new ValidationException(
                    "ENTIDAD_CORRELATIVO_INVALIDA",
                    "La entidad indicada para generar código no tiene un formato válido."
            );
        }

        if (normalized.length() > 80) {
            throw new ValidationException(
                    "ENTIDAD_CORRELATIVO_MUY_LARGA",
                    "La entidad del correlativo no debe superar 80 caracteres."
            );
        }

        return normalized;
    }

    private String normalizePrefix(String prefix) {
        if (!StringNormalizer.hasText(prefix)) {
            throw new ValidationException(
                    "PREFIJO_CORRELATIVO_REQUERIDO",
                    "Debe indicar el prefijo del código."
            );
        }

        String normalized = StringNormalizer
                .upperWithoutAccents(prefix)
                .replaceAll("[^A-Z0-9_-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        if (!normalized.matches("^[A-Z0-9][A-Z0-9_-]{1,29}$")) {
            throw new ValidationException(
                    "PREFIJO_CORRELATIVO_INVALIDO",
                    "El prefijo del correlativo no tiene un formato válido."
            );
        }

        return normalized;
    }

    private int normalizeLength(Integer length) {
        if (length == null) {
            return DEFAULT_LENGTH;
        }

        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new ValidationException(
                    "LONGITUD_CORRELATIVO_INVALIDA",
                    "La longitud del correlativo debe estar entre " + MIN_LENGTH + " y " + MAX_LENGTH + "."
            );
        }

        return length;
    }

    private String defaultPrefixFromEntidad(String entidad) {
        String normalized = normalizeEntidad(entidad)
                .replace("_", "")
                .toUpperCase(Locale.ROOT);

        if (normalized.length() <= 8) {
            return normalized;
        }

        return normalized.substring(0, 8);
    }

    private String resolveDescription(String entidad) {
        CodigoDefault defaults = DEFAULTS.get(entidad);

        if (defaults != null) {
            return defaults.descripcion();
        }

        return "Correlativo generado automáticamente para " + entidad;
    }

    private record CodigoDefault(
            String prefijo,
            int longitud,
            String descripcion
    ) {
    }
}