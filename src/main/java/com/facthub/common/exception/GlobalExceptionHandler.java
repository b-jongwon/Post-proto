package com.facthub.common.exception;

import com.facthub.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBusinessException(
            BusinessException exception
    ) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(
                        ApiResponse.failure(
                                exception.getErrorCode(),
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleDuplicateUser(
            DuplicateUserException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        exception.getErrorCode(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleInvalidLogin(
            InvalidLoginException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "INVALID_CREDENTIALS",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handlePostNotFound(
            PostNotFoundException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "POST_NOT_FOUND",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handlePostAccessDenied(
            PostAccessDeniedException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "POST_ACCESS_DENIED",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleCommentNotFound(
            CommentNotFoundException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "COMMENT_NOT_FOUND",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(CommentAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleCommentAccessDenied(
            CommentAccessDeniedException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "COMMENT_ACCESS_DENIED",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fields =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult()
                .getFieldErrors()) {

            fields.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiResponse<Void> response =
                ApiResponse.failure(
                        "VALIDATION_ERROR",
                        "입력값을 확인해주세요.",
                        fields
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleUnreadableBody(
            HttpMessageNotReadableException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "INVALID_REQUEST_BODY",
                        "요청 본문이 없거나 JSON 형식이 올바르지 않습니다."
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "INVALID_ARGUMENT",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleNoResourceFound(
            NoResourceFoundException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "API_NOT_FOUND",
                        "요청한 API를 찾을 수 없습니다."
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnexpected(
            Exception exception
    ) {
        log.error(
                "처리되지 않은 서버 예외가 발생했습니다.",
                exception
        );

        ApiResponse<Void> response =
                ApiResponse.failure(
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다."
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(response);
    }
}
