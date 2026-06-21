package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum S3ErrorCode implements ErrorCode {
    INVALID_FILE_KEY(HttpStatus.BAD_REQUEST, "S3-400-001", "유효하지 않은 파일 키입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    S3ErrorCode(HttpStatus httpStatus, String code, String message) {
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
