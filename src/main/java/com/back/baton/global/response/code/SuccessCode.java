package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    OK(HttpStatus.OK, "200-0", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "201-0", "요청에 성공했습니다."),

    USER_LOGIN_SUCCESS(HttpStatus.OK, "200-1", "로그인에 성공했습니다."),
    USER_SIGNUP_SUCCESS(HttpStatus.CREATED, "201-1", "회원가입에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
