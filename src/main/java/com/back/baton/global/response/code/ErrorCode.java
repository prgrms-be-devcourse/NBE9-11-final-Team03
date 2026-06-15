package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "400-1", "잘못된 입력값입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "400-2", "요청 필드 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500-1", "서버 내부 오류가 발생했습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "400-3", "요청 본문 형식이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "405-1", "지원하지 않는 HTTP 메서드입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404-1", "사용자를 찾을 수 없습니다."),
    DUPLICATED_USER(HttpStatus.CONFLICT, "409-1", "이미 존재하는 사용자입니다."),

    // Talent
    TALENT_NOT_FOUND(HttpStatus.NOT_FOUND, "404-2", "재능을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "404-3", "카테고리를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401-1", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "403-1", "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
