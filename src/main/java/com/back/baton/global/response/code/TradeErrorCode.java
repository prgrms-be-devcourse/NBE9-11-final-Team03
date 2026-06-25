package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum TradeErrorCode implements ErrorCode {
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE-404-001", "거래를 찾을 수 없습니다."),
    TRADE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "TRADE-403-001", "해당 거래에 접근할 권한이 없습니다."),
    TRADE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "TRADE-400-001", "이미 완료된 거래는 취소할 수 없습니다."),
    TRADE_ALREADY_CANCELLED(HttpStatus.CONFLICT, "TRADE-409-001", "이미 취소된 거래입니다."),
    TRADE_IN_DISPUTE(HttpStatus.BAD_REQUEST, "TRADE-400-002", "분쟁 중인 거래는 취소할 수 없습니다."),
    TRADE_UNDER_REVIEW(HttpStatus.BAD_REQUEST, "TRADE-400-003", "결과물 검토 중인 거래는 취소할 수 없습니다."),
    TRADE_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "TRADE-400-004", "진행 중인 거래가 아닙니다."),
    TRADE_NOT_UNDER_REVIEW(HttpStatus.BAD_REQUEST, "TRADE-400-005", "결과물 검토 중인 거래가 아닙니다."),
    TRADE_ALREADY_DISPUTED(HttpStatus.CONFLICT, "TRADE-409-002", "이미 분쟁이 신청된 거래입니다."),
    TRADE_SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE-404-002", "결과물 제출 내역을 찾을 수 없습니다."),
    TRADE_NOT_DISPUTED(HttpStatus.BAD_REQUEST, "TRADE-400-006", "분쟁 중인 거래가 아닙니다."),
    INVALID_DISPUTE_REASON(HttpStatus.BAD_REQUEST, "TRADE-400-007", "분쟁 사유는 5자 이상 200자 이하여야 합니다."),
    TRADE_NO_DISPUTES(HttpStatus.NOT_FOUND, "TRADE-404-003", "분쟁 중인 거래가 없습니다.");

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