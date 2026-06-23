package com.back.baton.global.response.code;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    OK(HttpStatus.OK, "200-0", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "201-0", "요청에 성공했습니다."),

    TALENT_CREATED(HttpStatus.CREATED, "201-2", "재능 등록에 성공했습니다."),
    TALENT_OK(HttpStatus.OK, "200-2", "재능 요청에 성공했습니다."),

    MATCH_PROPOSAL_CREATED(HttpStatus.CREATED, "201-3", "매칭 제안이 생성되었습니다."),
    MATCH_PROPOSAL_ACCEPTED(HttpStatus.OK, "200-4", "매칭 제안이 수락되었습니다."),
    MATCH_PROPOSAL_REJECTED(HttpStatus.OK, "200-5", "매칭 제안이 거절되었습니다."),
    MATCH_RECOMMENDATIONS_FOUND(HttpStatus.OK, "200-6", "매칭 추천 상대 목록 조회에 성공했습니다."),
    MATCH_RECOMMENDATION_DETAIL_FOUND(HttpStatus.OK, "200-7", "매칭 추천 상대 정보 조회에 성공했습니다."),
    MATCH_PROPOSALS_RECEIVED_FOUND(HttpStatus.OK, "200-14", "받은 매칭 제안 목록 조회에 성공했습니다."),
    MATCH_PROPOSALS_SENT_FOUND(HttpStatus.OK, "200-15", "보낸 매칭 제안 목록 조회에 성공했습니다."),

    TRADE_OK(HttpStatus.OK, "200-7", "거래 조회에 성공했습니다."),
    TRADE_CANCELLED(HttpStatus.OK, "200-8", "거래가 취소되었습니다."),
    TRADE_PRESIGNED_URL_CREATED(HttpStatus.CREATED, "201-4", "Presigned URL이 발급되었습니다."),
    TRADE_SUBMISSION_CREATED(HttpStatus.CREATED, "201-5", "결과물이 제출되었습니다."),
    TRADE_SUBMISSION_OK(HttpStatus.OK, "200-9", "결과물 조회에 성공했습니다."),
    TRADE_COMPLETED(HttpStatus.OK, "200-10", "거래가 완료되었습니다."),
    TRADE_DISPUTED(HttpStatus.OK, "200-11", "분쟁이 신청되었습니다."),
    TRADE_DISPUTE_RESOLVED(HttpStatus.OK, "200-16", "분쟁이 처리되었습니다."),

    TALENT_ATTACHMENT_CREATED(HttpStatus.CREATED, "201-4", "재능 첨부 등록에 성공했습니다."),
    TALENT_ATTACHMENT_OK(HttpStatus.OK, "200-3", "재능 첨부 요청에 성공했습니다."),

    CHAT_ROOM_CREATED(HttpStatus.OK, "200-10", "채팅방 생성 또는 조회에 성공했습니다."),
    CHAT_MESSAGE_SENT(HttpStatus.OK, "200-11", "채팅 메시지 전송에 성공했습니다."),
    CHAT_MESSAGES_FOUND(HttpStatus.OK, "200-12", "채팅 메시지 목록 조회에 성공했습니다."),
    CHAT_ROOM_FOUND(HttpStatus.OK, "200-13", "채팅방 조회에 성공했습니다."),

    USER_LOGIN_SUCCESS(HttpStatus.OK, "200-1", "로그인에 성공했습니다."),
    USER_REISSUE_SUCCESS(HttpStatus.OK, "200-12", "토큰 재발행에 성공했습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "200-3","로그아웃에 성공했습니다."),
    USER_WITHDRAW_SUCCESS(HttpStatus.OK, "200-11","탈퇴에 성공했습니다."),
    USER_SIGNUP_SUCCESS(HttpStatus.CREATED, "201-1", "회원가입에 성공했습니다."),

    CATEGORY_OK(HttpStatus.OK, "200-13", "카테고리 조회에 성공했습니다.");

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