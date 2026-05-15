package com.upsjb.ms3.shared.audit;

import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.shared.constants.SystemActors;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

public record AuditContext(
        Long idUsuarioActorMs1,
        Long idEmpleadoActorMs2,
        String username,
        String email,
        RolSistema rolActor,
        String sessionId,
        String ipAddress,
        String userAgent,
        String httpMethod,
        String requestPath,
        String queryString,
        String requestId,
        String correlationId,
        LocalDateTime createdAt,
        Map<String, Object> metadata
) {

    public AuditContext {
        username = clean(username, 120);
        email = clean(email, 180);
        sessionId = clean(sessionId, 120);
        ipAddress = clean(ipAddress, 80);
        userAgent = clean(userAgent, 500);
        httpMethod = clean(httpMethod, 20);
        requestPath = clean(requestPath, 500);
        queryString = clean(queryString, 1000);
        requestId = clean(requestId, 100);
        correlationId = clean(correlationId, 100);

        if (rolActor == null) {
            rolActor = RolSistema.ANONIMO;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public static AuditContext empty() {
        return builder().build();
    }

    public static AuditContext system() {
        return builder()
                .username(SystemActors.SYSTEM)
                .rolActor(RolSistema.SISTEMA)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasActor() {
        return idUsuarioActorMs1 != null
                || StringUtils.hasText(username)
                || StringUtils.hasText(email);
    }

    public String actorLabel() {
        if (StringUtils.hasText(username)) {
            return username;
        }

        if (StringUtils.hasText(email)) {
            return email;
        }

        if (idUsuarioActorMs1 != null) {
            return "USUARIO_MS1_" + idUsuarioActorMs1;
        }

        return SystemActors.ANONYMOUS;
    }

    public Map<String, Object> safeMetadata() {
        Map<String, Object> result = new LinkedHashMap<>(metadata);
        result.putIfAbsent("actor", actorLabel());
        result.putIfAbsent("rolActor", rolActor.getCode());
        result.putIfAbsent("requestId", requestId);
        result.putIfAbsent("correlationId", correlationId);
        result.putIfAbsent("path", requestPath);
        result.putIfAbsent("method", httpMethod);
        return Collections.unmodifiableMap(result);
    }

    private static String clean(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String sanitized = value.trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");

        return sanitized.substring(0, Math.min(sanitized.length(), maxLength));
    }

    public static final class Builder {

        private Long idUsuarioActorMs1;
        private Long idEmpleadoActorMs2;
        private String username;
        private String email;
        private RolSistema rolActor = RolSistema.ANONIMO;
        private String sessionId;
        private String ipAddress;
        private String userAgent;
        private String httpMethod;
        private String requestPath;
        private String queryString;
        private String requestId;
        private String correlationId;
        private LocalDateTime createdAt;
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder idUsuarioActorMs1(Long idUsuarioActorMs1) {
            this.idUsuarioActorMs1 = idUsuarioActorMs1;
            return this;
        }

        public Builder idEmpleadoActorMs2(Long idEmpleadoActorMs2) {
            this.idEmpleadoActorMs2 = idEmpleadoActorMs2;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder rolActor(RolSistema rolActor) {
            this.rolActor = rolActor;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder metadata(String key, Object value) {
            if (StringUtils.hasText(key) && value != null) {
                this.metadata.put(key.trim(), value);
            }
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                metadata.forEach(this::metadata);
            }
            return this;
        }

        public AuditContext build() {
            return new AuditContext(
                    idUsuarioActorMs1,
                    idEmpleadoActorMs2,
                    username,
                    email,
                    rolActor,
                    sessionId,
                    ipAddress,
                    userAgent,
                    httpMethod,
                    requestPath,
                    queryString,
                    requestId,
                    correlationId,
                    createdAt,
                    metadata
            );
        }
    }
}