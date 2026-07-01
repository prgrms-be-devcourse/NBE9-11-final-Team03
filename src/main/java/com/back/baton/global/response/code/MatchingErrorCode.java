package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum MatchingErrorCode implements ErrorCode {

    SELF_MATCHING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "MATCHING-400-001", "자기 자신에게 매칭을 제안할 수 없습니다."),
    INVALID_MATCHING_STATUS(HttpStatus.BAD_REQUEST, "MATCHING-400-002", "처리할 수 없는 매칭 상태입니다."),
    SWAP_ACCEPT_NOT_IMPLEMENTED(HttpStatus.BAD_REQUEST, "MATCHING-400-003", "양방향 거래 수락은 아직 지원되지 않습니다."),
    WANT_TALENT_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "MATCHING-400-004", "교환하고 싶은 재능 카테고리를 먼저 등록해주세요."),
    MATCH_PROPOSAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MATCHING-403-001", "해당 매칭 제안을 처리할 권한이 없습니다."),
    MATCH_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING-404-001", "매칭 제안을 찾을 수 없습니다."),
    DUPLICATED_MATCHING_PROPOSAL(HttpStatus.CONFLICT, "MATCHING-409-001", "이미 진행 중인 매칭 제안이 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MatchingErrorCode(HttpStatus httpStatus, String code, String message) {
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
