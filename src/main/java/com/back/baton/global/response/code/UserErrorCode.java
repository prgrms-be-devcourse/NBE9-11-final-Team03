package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404-001", "사용자를 찾을 수 없습니다."),

    DUPLICATED_USER(HttpStatus.CONFLICT, "USER-409-001", "이미 존재하는 사용자입니다."),
    UNUSABLE_EMAIL(HttpStatus.CONFLICT, "USER-409-002", "가입할 수 없는 이메일입니다."),

    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "USER-400-001", "잘못된 비밀번호 양식입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER-400-002", "잘못된 비밀번호입니다."),
    ESCROW_IN_PROGRESS(HttpStatus.BAD_REQUEST, "USER-400-003", "진행 중인 거래가 존재합니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "USER-400-004", "잘못된 이메일입니다."),

    DORMANT_STATUS(HttpStatus.UNAUTHORIZED,"USER-401-001", "휴면 처리된 계정입니다."),
    SUSPENDED_STATUS(HttpStatus.UNAUTHORIZED,"USER-401-002", "정지 처리된 계정입니다."),
    WITHDRAWN_STATUS(HttpStatus.UNAUTHORIZED,"USER-401-003", "탈퇴 처리된 계정입니다."),
    BANNED_STATUS(HttpStatus.UNAUTHORIZED,"USER-401-004", "영구정지 처리된 계정입니다."),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String code, String message) {
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
