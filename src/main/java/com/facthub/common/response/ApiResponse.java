package com.facthub.common.response;

import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null
        );
    }

    public static ApiResponse<Void> failure(
            String code,
            String message
    ) {
        return new ApiResponse<>(
                false,
                null,
                new ErrorDetail(code, message, Map.of())
        );
    }

    public static ApiResponse<Void> failure(
            String code,
            String message,
            Map<String, String> fields
    ) {
        return new ApiResponse<>(
                false,
                null,
                new ErrorDetail(code, message, fields)
        );
    }

    public record ErrorDetail(
            String code,
            String message,
            Map<String, String> fields
    ) {
    }
}