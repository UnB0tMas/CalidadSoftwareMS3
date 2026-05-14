// ruta: src/main/java/com/upsjb/ms3/integration/ms2/Ms2EmpleadoSnapshotClientImpl.java
package com.upsjb.ms3.integration.ms2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.config.Ms2IntegrationProperties;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotClientImpl implements Ms2EmpleadoSnapshotClient {

    private static final String OP_FIND_BY_USUARIO = "FIND_EMPLEADO_BY_ID_USUARIO_MS1";
    private static final String OP_FIND_BY_EMPLEADO = "FIND_EMPLEADO_BY_ID_EMPLEADO_MS2";
    private static final String OP_REQUEST_SNAPSHOT = "REQUEST_EMPLEADO_SNAPSHOT";
    private static final String OP_UPSERT_SNAPSHOT = "UPSERT_EMPLEADO_SNAPSHOT";

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final Ms2IntegrationProperties properties;
    private final Ms2ClientErrorMapper errorMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<EmpleadoSnapshotResponse> findByIdUsuarioMs1(Long idUsuarioMs1) {
        validateId(idUsuarioMs1, "idUsuarioMs1", OP_FIND_BY_USUARIO);
        ensureEnabled(OP_FIND_BY_USUARIO);

        try {
            URI uri = resolveUriWithQuery(properties.empleadoByUsuarioUri(), "idUsuarioMs1", idUsuarioMs1);

            Map<String, Object> body = restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(MAP_TYPE);

            return Optional.ofNullable(toSnapshotResponse(body));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }

            throw errorMapper.map(ex, OP_FIND_BY_USUARIO);
        } catch (RuntimeException ex) {
            throw errorMapper.map(ex, OP_FIND_BY_USUARIO);
        }
    }

    @Override
    public Optional<EmpleadoSnapshotResponse> findByIdEmpleadoMs2(Long idEmpleadoMs2) {
        validateId(idEmpleadoMs2, "idEmpleadoMs2", OP_FIND_BY_EMPLEADO);
        ensureEnabled(OP_FIND_BY_EMPLEADO);

        try {
            URI uri = resolveUriWithQuery(properties.empleadoSnapshotUri(), "idEmpleadoMs2", idEmpleadoMs2);

            Map<String, Object> body = restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(MAP_TYPE);

            return Optional.ofNullable(toSnapshotResponse(body));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }

            throw errorMapper.map(ex, OP_FIND_BY_EMPLEADO);
        } catch (RuntimeException ex) {
            throw errorMapper.map(ex, OP_FIND_BY_EMPLEADO);
        }
    }

    @Override
    public EmpleadoSnapshotResponse requestSnapshotByIdUsuarioMs1(Long idUsuarioMs1) {
        return findByIdUsuarioMs1(idUsuarioMs1)
                .orElseThrow(() -> errorMapper.notFound(OP_REQUEST_SNAPSHOT));
    }

    @Override
    public EmpleadoSnapshotResponse upsertSnapshot(EmpleadoSnapshotRequest request) {
        validateRequest(request);
        ensureEnabled(OP_UPSERT_SNAPSHOT);

        try {
            Map<String, Object> body = restClient()
                    .post()
                    .uri(properties.empleadoSnapshotUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MAP_TYPE);

            EmpleadoSnapshotResponse response = toSnapshotResponse(body);
            if (response == null) {
                throw errorMapper.invalidRequest("MS2 devolvió una respuesta vacía al registrar snapshot.", OP_UPSERT_SNAPSHOT);
            }

            return response;
        } catch (RuntimeException ex) {
            throw errorMapper.map(ex, OP_UPSERT_SNAPSHOT);
        }
    }

    private RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = timeoutMillis(properties.getTimeout());
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(properties.getInternalServiceKeyHeader(), properties.getInternalServiceKey())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private URI resolveUriWithQuery(URI baseUri, String paramName, Object value) {
        String template = baseUri.toString();

        if (template.contains("{" + paramName + "}")) {
            return UriComponentsBuilder
                    .fromUriString(template)
                    .buildAndExpand(Map.of(paramName, value))
                    .toUri();
        }

        return UriComponentsBuilder
                .fromUri(baseUri)
                .queryParam(paramName, value)
                .build(true)
                .toUri();
    }

    private void ensureEnabled(String operation) {
        if (!properties.isEnabled()) {
            throw errorMapper.integrationDisabled(operation);
        }

        if (properties.getBaseUrl() == null) {
            throw errorMapper.invalidRequest("La URL base de MS2 no está configurada.", operation);
        }

        if (!StringUtils.hasText(properties.getInternalServiceKeyHeader())
                || !StringUtils.hasText(properties.getInternalServiceKey())) {
            throw errorMapper.invalidRequest("La clave interna de integración con MS2 no está configurada.", operation);
        }
    }

    private void validateId(Long id, String field, String operation) {
        if (id == null || id <= 0) {
            throw errorMapper.invalidRequest("El campo " + field + " debe ser mayor a cero.", operation);
        }
    }

    private void validateRequest(EmpleadoSnapshotRequest request) {
        if (request == null) {
            throw errorMapper.invalidRequest("El snapshot de empleado MS2 es obligatorio.", OP_UPSERT_SNAPSHOT);
        }

        validateId(request.idEmpleadoMs2(), "idEmpleadoMs2", OP_UPSERT_SNAPSHOT);
        validateId(request.idUsuarioMs1(), "idUsuarioMs1", OP_UPSERT_SNAPSHOT);

        if (!StringUtils.hasText(request.codigoEmpleado())) {
            throw errorMapper.invalidRequest("El código del empleado es obligatorio.", OP_UPSERT_SNAPSHOT);
        }

        if (!StringUtils.hasText(request.nombresCompletos())) {
            throw errorMapper.invalidRequest("Los nombres completos del empleado son obligatorios.", OP_UPSERT_SNAPSHOT);
        }

        if (request.empleadoActivo() == null) {
            throw errorMapper.invalidRequest("Debe indicar si el empleado está activo.", OP_UPSERT_SNAPSHOT);
        }
    }

    private EmpleadoSnapshotResponse toSnapshotResponse(Map<String, Object> body) {
        Object data = unwrapData(body);
        if (data == null) {
            return null;
        }

        return objectMapper.convertValue(data, EmpleadoSnapshotResponse.class);
    }

    private Object unwrapData(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        for (String key : new String[]{"data", "payload", "result", "empleado", "snapshot"}) {
            Object value = body.get(key);
            if (value != null) {
                return value;
            }
        }

        Map<String, Object> direct = objectMapper.convertValue(body, new TypeReference<>() {
        });

        if (direct.containsKey("idEmpleadoMs2") || direct.containsKey("idUsuarioMs1")) {
            return direct;
        }

        return null;
    }

    private int timeoutMillis(Duration timeout) {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            return 5000;
        }

        long millis = timeout.toMillis();
        if (millis > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return Math.max(1, (int) millis);
    }
}