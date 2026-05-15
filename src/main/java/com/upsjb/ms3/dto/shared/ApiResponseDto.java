// ruta: src/main/java/com/upsjb/ms3/dto/shared/ApiResponseDto.java
package com.upsjb.ms3.dto.shared;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ApiResponseDto<T>(
        Boolean success,
        String code,
        String message,
        T data,
        LocalDateTime timestamp,
        String requestId,
        String correlationId,
        String path
) {

    public static <T> ApiResponseDto<T> ok(
            String message,
            T data,
            String requestId,
            String correlationId,
            String path
    ) {
        return new ApiResponseDto<>(
                Boolean.TRUE,
                "OK",
                message,
                data,
                LocalDateTime.now(),
                requestId,
                correlationId,
                path
        );
    }

    public static <T> ApiResponseDto<T> created(
            String message,
            T data,
            String requestId,
            String correlationId,
            String path
    ) {
        return new ApiResponseDto<>(
                Boolean.TRUE,
                "CREATED",
                message,
                data,
                LocalDateTime.now(),
                requestId,
                correlationId,
                path
        );
    }

    public static <T> ApiResponseDto<T> fail(
            String code,
            String message,
            T data,
            String requestId,
            String correlationId,
            String path
    ) {
        return new ApiResponseDto<>(
                Boolean.FALSE,
                code,
                message,
                data,
                LocalDateTime.now(),
                requestId,
                correlationId,
                path
        );
    }
}