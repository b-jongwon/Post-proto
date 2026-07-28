package com.facthub.factcheck.exception;

import org.springframework.http.HttpStatus;

public class FactCheckException
        extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public FactCheckException(
            String errorCode,
            String message,
            HttpStatus httpStatus
    ) {
        super(message);

        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /*
     * 분석 결과 또는 대표 분석 없음
     */
    public static FactCheckException notFound() {
        return new FactCheckException(
                "FACT_CHECK_NOT_FOUND",
                "팩트체크 결과를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND
        );
    }

    /*
     * 같은 게시글을 이미 분석 중
     */
    public static FactCheckException alreadyRunning() {
        return new FactCheckException(
                "ANALYSIS_ALREADY_RUNNING",
                "해당 게시글의 AI 분석이 이미 진행 중입니다.",
                HttpStatus.CONFLICT
        );
    }

    /*
     * 대표 분석으로 선택할 수 없는 분석
     */
    public static FactCheckException invalidSelection() {
        return new FactCheckException(
                "INVALID_ANALYSIS_SELECTION",
                "해당 분석을 대표 분석으로 지정할 수 없습니다.",
                HttpStatus.BAD_REQUEST
        );
    }

    /*
     * Gemini 환경변수 누락
     */
    public static FactCheckException notConfigured() {
        return new FactCheckException(
                "GEMINI_NOT_CONFIGURED",
                "Gemini API 키가 설정되지 않았습니다.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /*
     * Gemini API 또는 외부 AI 호출 실패
     */
    public static FactCheckException upstream(
            String message
    ) {
        return new FactCheckException(
                "GEMINI_API_ERROR",
                message,
                HttpStatus.BAD_GATEWAY
        );
    }

    /*
     * Gemini 응답 형식 오류
     */
    public static FactCheckException invalidResponse() {
        return new FactCheckException(
                "INVALID_GEMINI_RESPONSE",
                "Gemini 분석 결과의 형식이 올바르지 않습니다.",
                HttpStatus.BAD_GATEWAY
        );
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}