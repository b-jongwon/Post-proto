package com.facthub.factcheck.exception;

import com.facthub.common.response.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class FactCheckExceptionHandler {

    @ExceptionHandler(FactCheckException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleFactCheckException(
            FactCheckException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        exception.getErrorCode(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(exception.getHttpStatus())
                .body(response);
    }
}