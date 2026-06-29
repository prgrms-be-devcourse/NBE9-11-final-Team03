package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-400-001", "잘못된 입력값입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON-400-002", "요청 필드 검증에 실패했습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON-400-003", "요청 본문 형식이 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON-403-001", "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-405-001", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500-001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
