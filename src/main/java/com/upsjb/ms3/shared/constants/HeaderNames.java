package com.upsjb.ms3.shared.constants;

public final class HeaderNames {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";

    public static final String FORWARDED_FOR = "X-Forwarded-For";
    public static final String FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String FORWARDED_HOST = "X-Forwarded-Host";
    public static final String FORWARDED_PORT = "X-Forwarded-Port";
    public static final String REAL_IP = "X-Real-IP";

    public static final String USER_AGENT = "User-Agent";
    public static final String GATEWAY_SOURCE = "X-Gateway-Source";
    public static final String INTERNAL_SERVICE_KEY = "X-Internal-Service-Key";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String ORIGIN = "Origin";

    private HeaderNames() {
    }
}