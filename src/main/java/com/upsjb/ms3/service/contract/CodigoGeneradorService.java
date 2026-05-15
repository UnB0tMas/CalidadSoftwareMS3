// ruta: src/main/java/com/upsjb/ms3/service/contract/CodigoGeneradorService.java
package com.upsjb.ms3.service.contract;

public interface CodigoGeneradorService {

    String generarCodigoProducto();

    String generarCodigoSku();

    String generarCodigoPromocion();

    String generarCodigoCompra();

    String generarCodigoReservaStock();

    String generarCodigoMovimientoInventario();

    String generarCodigo(String entidad);

    String generarCodigo(String entidad, String prefijoPorDefecto, int longitudPorDefecto);
}