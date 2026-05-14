package com.upsjb.ms3.shared.constants;

import java.time.ZoneId;

public final class Ms3Constants {

    public static final String SERVICE_CODE = "MS3";
    public static final String SERVICE_NAME = "ms-catalogo-inventario";
    public static final String SERVICE_DISPLAY_NAME = "MS Catalogo Inventario";

    public static final String DEFAULT_LOCALE = "es-PE";
    public static final String DEFAULT_TIMEZONE = "America/Lima";
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("America/Lima");

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    public static final int MAX_REQUEST_ID_LENGTH = 100;
    public static final int MAX_CORRELATION_ID_LENGTH = 100;
    public static final int MAX_USER_AGENT_LENGTH = 500;
    public static final int MAX_IP_LENGTH = 80;
    public static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    public static final String DEFAULT_CURRENCY = "PEN";

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    private Ms3Constants() {
    }
}