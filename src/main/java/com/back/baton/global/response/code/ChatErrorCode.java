package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum ChatErrorCode implements ErrorCode {

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT-404-001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT-403-001", "해당 채팅방에 접근할 권한이 없습니다."),
    SELF_CHAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHAT-400-001", "자기 자신과는 채팅할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ChatErrorCode(HttpStatus httpStatus, String code, String message) {
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