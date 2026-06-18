package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum TokenErrorCode implements ErrorCode {

    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN-400-001", "잘못된 토큰입니다"),
    REUSED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN-400-002", "재사용된 토큰입니다"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN-404-001", "리프레시 토큰을 찾을 수 없습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TokenErrorCode(HttpStatus httpStatus, String code, String message) {
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
