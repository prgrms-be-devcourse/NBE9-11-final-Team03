package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum CreditErrorCode implements ErrorCode {
    CREDIT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "CREDIT-404-001", "크레딧 계좌를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CreditErrorCode(HttpStatus httpStatus, String code, String message) {
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