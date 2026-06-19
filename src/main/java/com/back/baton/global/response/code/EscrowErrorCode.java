package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum EscrowErrorCode implements ErrorCode {

    ESCROW_NOT_FOUND(HttpStatus.NOT_FOUND, "ESCROW-404-001", "에스크로를 찾을 수 없습니다."),
    INVALID_ESCROW_STATUS(HttpStatus.CONFLICT, "ESCROW-409-001", "현재 에스크로 상태에서는 해당 작업을 수행할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    EscrowErrorCode(HttpStatus httpStatus, String code, String message) {
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