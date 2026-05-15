// ruta: src/main/java/com/upsjb/ms3/integration/ms4/Ms4StockSyncClientImpl.java
package com.upsjb.ms3.integration.ms4;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.config.Ms4IntegrationProperties;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class Ms4StockSyncClientImpl implements Ms4StockSyncClient {

    private static final String OP_SEND_STOCK_SYNC = "SEND_STOCK_SYNC_TO_MS4";
    private static final String OP_FETCH_PENDING_EVENTS = "FETCH_PENDING_STOCK_EVENTS_FROM_MS4";

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final Ms4IntegrationProperties properties;
    private final Ms4ClientErrorMapper errorMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Ms4StockSyncResponse sendStockSync(Ms4StockSyncRequest request) {
        validateSyncRequest(request);
        ensureEnabled(OP_SEND_STOCK_SYNC);

        try {
            Map<String, Object> body = restClient()
                    .post()
                    .uri(properties.stockSyncUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MAP_TYPE);

            return toSyncResponse(body);
        } catch (RuntimeException ex) {
            throw errorMapper.map(ex, OP_SEND_STOCK_SYNC);
        }
    }

    @Override
    public List<Ms4PendingStockEvent> fetchPendingStockEvents(Integer limit) {
        ensureEnabled(OP_FETCH_PENDING_EVENTS);

        try {
            URI uri = UriComponentsBuilder
                    .fromUri(properties.pendingStockEventsUri())
                    .queryParam("limit", normalizeLimit(limit))
                    .build(true)
                    .toUri();

            Object body = restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(Object.class);

            return toPendingEvents(body);
        } catch (RuntimeException ex) {
            throw errorMapper.map(ex, OP_FETCH_PENDING_EVENTS);
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

    private void ensureEnabled(String operation) {
        if (!properties.isEnabled()) {
            throw errorMapper.integrationDisabled(operation);
        }

        if (properties.getBaseUrl() == null) {
            throw errorMapper.invalidRequest("La URL base de MS4 no está configurada.", operation);
        }

        if (!StringUtils.hasText(properties.getInternalServiceKeyHeader())
                || !StringUtils.hasText(properties.getInternalServiceKey())) {
            throw errorMapper.invalidRequest("La clave interna de integración con MS4 no está configurada.", operation);
        }
    }

    private void validateSyncRequest(Ms4StockSyncRequest request) {
        if (request == null) {
            throw errorMapper.invalidRequest("La solicitud de sincronización con MS4 es obligatoria.", OP_SEND_STOCK_SYNC);
        }

        if (!StringUtils.hasText(request.eventType())) {
            throw errorMapper.invalidRequest("El tipo de evento es obligatorio para sincronizar con MS4.", OP_SEND_STOCK_SYNC);
        }

        if (!StringUtils.hasText(request.operation())) {
            throw errorMapper.invalidRequest("La operación de stock es obligatoria para sincronizar con MS4.", OP_SEND_STOCK_SYNC);
        }

        if (!StringUtils.hasText(request.referenceExternalId())) {
            throw errorMapper.invalidRequest("La referencia externa de MS4 es obligatoria.", OP_SEND_STOCK_SYNC);
        }
    }

    private Ms4StockSyncResponse toSyncResponse(Map<String, Object> body) {
        Object data = unwrapData(body);

        if (data == null) {
            return new Ms4StockSyncResponse(
                    true,
                    "ACCEPTED",
                    "MS4 aceptó la solicitud sin cuerpo de respuesta.",
                    null,
                    null,
                    body == null ? Map.of() : body
            );
        }

        if (data instanceof Map<?, ?> map) {
            Map<String, Object> normalized = normalizeMap(map);

            Boolean accepted = asBoolean(firstNonNull(
                    normalized.get("accepted"),
                    normalized.get("aceptado"),
                    normalized.get("success")
            ));

            return new Ms4StockSyncResponse(
                    accepted == null || accepted,
                    asString(firstNonNull(normalized.get("status"), normalized.get("estado"))),
                    asString(firstNonNull(normalized.get("message"), normalized.get("mensaje"))),
                    asString(firstNonNull(normalized.get("externalOperationId"), normalized.get("operationId"))),
                    asString(normalized.get("correlationId")),
                    normalized
            );
        }

        return objectMapper.convertValue(data, Ms4StockSyncResponse.class);
    }

    private List<Ms4PendingStockEvent> toPendingEvents(Object body) {
        Object data = body;

        if (body instanceof Map<?, ?> map) {
            data = unwrapData(normalizeMap(map));
        }

        if (data == null) {
            return List.of();
        }

        if (data instanceof List<?> list) {
            List<Ms4PendingStockEvent> result = new ArrayList<>();

            for (Object item : list) {
                result.add(objectMapper.convertValue(item, Ms4PendingStockEvent.class));
            }

            return List.copyOf(result);
        }

        List<Map<String, Object>> converted = objectMapper.convertValue(data, new TypeReference<>() {
        });

        List<Ms4PendingStockEvent> result = new ArrayList<>();
        for (Map<String, Object> item : converted) {
            result.add(objectMapper.convertValue(item, Ms4PendingStockEvent.class));
        }

        return List.copyOf(result);
    }

    private Object unwrapData(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        for (String key : new String[]{"data", "payload", "result", "events", "items", "content"}) {
            Object value = body.get(key);
            if (value != null) {
                return value;
            }
        }

        return body;
    }

    private Map<String, Object> normalizeMap(Map<?, ?> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        return objectMapper.convertValue(raw, new TypeReference<>() {
        });
    }

    private Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }

        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 50;
        }

        return Math.min(limit, 500);
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