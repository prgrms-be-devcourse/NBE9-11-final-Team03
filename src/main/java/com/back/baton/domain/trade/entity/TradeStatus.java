package com.back.baton.domain.trade.entity;

public enum TradeStatus {
    REQUESTED,     // 거래 생성 직후
    ACCEPTED,      // 예치 완료 / 거래 시작 가능
    IN_PROGRESS,   // 작업 진행 중
    UNDER_REVIEW,  // 결과물 검토 중
    COMPLETED,     // 구매 확정 완료
    CANCELLED,     // 거래 취소
    DISPUTED       // 분쟁 진행 중
}