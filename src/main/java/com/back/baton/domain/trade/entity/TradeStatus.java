package com.back.baton.domain.trade.entity;

public enum TradeStatus {
    IN_PROGRESS,   // 작업 진행 중
    UNDER_REVIEW,  // 결과물 검토 중
    AWAITING_PARTNER,  // 상대방 확정 대기 중
    COMPLETED,     // 구매 확정 완료
    CANCELLED,     // 거래 취소
    DISPUTED       // 분쟁 진행 중
}