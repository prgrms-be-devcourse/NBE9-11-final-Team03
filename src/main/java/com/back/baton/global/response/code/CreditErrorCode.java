package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum CreditErrorCode implements ErrorCode {
    CREDIT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "CREDIT-404-001", "크레딧 계좌를 찾을 수 없습니다."),
    CREDIT_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "CREDIT-409-001", "이미 크레딧 계좌가 존재합니다."),
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, "CREDIT-400-001", "크레딧 금액은 0보다 커야 합니다."),
    INSUFFICIENT_CREDIT_BALANCE(HttpStatus.BAD_REQUEST, "CREDIT-400-002", "크레딧 잔액이 부족합니다."),
    DUPLICATE_ESCROW_HOLD_REQUEST(HttpStatus.CONFLICT, "CREDIT-409-002", "이미 처리 중인 에스크로 예치 요청입니다."),
    DUPLICATE_ESCROW_REFUND_REQUEST(HttpStatus.CONFLICT, "CREDIT-409-003", "이미 처리 중인 에스크로 환불 요청입니다."),
    INSUFFICIENT_ESCROW_BALANCE(HttpStatus.BAD_REQUEST, "CREDIT-400-003", "에스크로 잔액이 부족합니다."),
    DUPLICATE_ESCROW_SETTLE_REQUEST(HttpStatus.CONFLICT, "CREDIT-409-004", "이미 처리된 에스크로 정산 요청입니다.");

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