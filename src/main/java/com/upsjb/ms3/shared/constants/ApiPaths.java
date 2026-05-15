package com.upsjb.ms3.shared.constants;

public final class ApiPaths {

    public static final String API = "/api";
    public static final String MS3 = API + "/ms3";

    public static final String PUBLIC = MS3 + "/public";
    public static final String ADMIN = MS3 + "/admin";
    public static final String CATALOGO = MS3 + "/catalogo";
    public static final String INVENTARIO = MS3 + "/inventario";
    public static final String OUTBOX = MS3 + "/outbox";
    public static final String AUDITORIA = MS3 + "/auditoria";
    public static final String INTERNAL = "/api/internal";

    public static final String PUBLIC_PATTERN = PUBLIC + "/**";
    public static final String ADMIN_PATTERN = ADMIN + "/**";
    public static final String CATALOGO_PATTERN = CATALOGO + "/**";
    public static final String INVENTARIO_PATTERN = INVENTARIO + "/**";
    public static final String OUTBOX_PATTERN = OUTBOX + "/**";
    public static final String AUDITORIA_PATTERN = AUDITORIA + "/**";
    public static final String INTERNAL_PATTERN = INTERNAL + "/**";

    public static final String SWAGGER_UI = "/swagger-ui.html";
    public static final String SWAGGER_UI_PATTERN = "/swagger-ui/**";
    public static final String API_DOCS_PATTERN = "/v3/api-docs/**";

    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_HEALTH_PATTERN = "/actuator/health/**";
    public static final String ACTUATOR_INFO = "/actuator/info";

    public static final String PRODUCTOS_PUBLICOS = PUBLIC + "/productos";
    public static final String CATALOGO_PUBLICO = PUBLIC + "/catalogo";
    public static final String PROMOCIONES_PUBLICAS = PUBLIC + "/promociones";

    private ApiPaths() {
    }
}