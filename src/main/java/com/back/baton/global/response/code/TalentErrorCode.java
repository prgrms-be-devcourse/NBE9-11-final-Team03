package com.back.baton.global.response.code;

import com.back.baton.global.response.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TalentErrorCode implements ErrorCode {

    TALENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TALENT-404-001", "재능을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "TALENT-404-002", "카테고리를 찾을 수 없습니다."),
    CATEGORY_INACTIVE(HttpStatus.BAD_REQUEST, "TALENT-400-001", "비활성화된 카테고리입니다."),
    TALENT_FORBIDDEN(HttpStatus.FORBIDDEN, "TALENT-403-001", "본인의 재능만 수정/삭제할 수 있습니다."),

    TALENT_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "TALENT-400-002", "진행 중인 거래가 있는 재능은 삭제할 수 없습니다."),


    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TALENT-404-003", "첨부를 찾을 수 없습니다."),
    ATTACHMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "TALENT-403-002", "본인의 재능에만 첨부를 등록/삭제할 수 있습니다."),

    TALENT_REGISTRATION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "TALENT-409-001", "재능 등록 개수 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TalentErrorCode(HttpStatus httpStatus, String code, String message) {
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