package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum AdminErrorCode implements ErrorCode {
    INVALID_ADMIN_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "ADMIN-400-001", "관리자 상태 변경 요청이 올바르지 않습니다."),
    SELF_STATUS_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "ADMIN-403-001", "관리자는 자기 자신의 상태를 변경할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN-404-001", "신고 내역을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AdminErrorCode(HttpStatus httpStatus, String code, String message) {
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
