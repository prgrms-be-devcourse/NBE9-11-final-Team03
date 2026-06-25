package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum ProfileErrorCode implements ErrorCode {
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE-404-001", "프로필을 찾을 수 없습니다."),
    INVALID_INTRODUCTION(HttpStatus.BAD_REQUEST, "PROFILE-400-001", "설명은 5자 이상이어야 합니다."),
    INVALID_CATEGORIES(HttpStatus.BAD_REQUEST, "PROFILE-400-002", "활성화된 카테고리만 선택해주세요"),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ProfileErrorCode(HttpStatus httpStatus, String code, String message) {
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
