package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum TradeErrorCode implements ErrorCode {
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE-404-001", "거래를 찾을 수 없습니다."),
    TRADE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "TRADE-403-001", "해당 거래에 접근할 권한이 없습니다."),
    TRADE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "TRADE-400-001", "이미 완료된 거래는 취소할 수 없습니다."),
    TRADE_ALREADY_CANCELLED(HttpStatus.CONFLICT, "TRADE-409-001", "이미 취소된 거래입니다."),
    TRADE_IN_DISPUTE(HttpStatus.BAD_REQUEST, "TRADE-400-002", "분쟁 중인 거래는 취소할 수 없습니다."),
    TRADE_UNDER_REVIEW(HttpStatus.BAD_REQUEST, "TRADE-400-003", "결과물 검토 중인 거래는 취소할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TradeErrorCode(HttpStatus httpStatus, String code, String message) {
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