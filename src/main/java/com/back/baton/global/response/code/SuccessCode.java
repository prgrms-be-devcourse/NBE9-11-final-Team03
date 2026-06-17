package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    OK(HttpStatus.OK, "200-0", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "201-0", "요청에 성공했습니다."),

    TALENT_CREATED(HttpStatus.CREATED, "201-2", "재능 등록에 성공했습니다."),
    TALENT_OK(HttpStatus.OK, "200-2", "재능 요청에 성공했습니다."),

    MATCH_PROPOSAL_CREATED(HttpStatus.CREATED, "201-3", "매칭 제안이 생성되었습니다."),
    MATCH_PROPOSAL_ACCEPTED(HttpStatus.OK, "200-4", "매칭 제안이 수락되었습니다."),

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
