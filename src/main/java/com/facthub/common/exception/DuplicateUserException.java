package com.facthub.common.exception;

public class DuplicateUserException extends RuntimeException {

    private final String errorCode;

    public DuplicateUserException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}