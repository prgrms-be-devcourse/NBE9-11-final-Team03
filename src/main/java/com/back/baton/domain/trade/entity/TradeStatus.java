package com.back.baton.domain.trade.entity;

public enum TradeStatus {
    REQUESTED,    // 거래 요청됨 (생성 시 초기 상태)
    IN_PROGRESS,  // 판매자 작업 진행 중
    IN_REVIEW,    // 구매자 검토 대기 중
    COMPLETED,    // 거래 완료
    CANCELLED,    // 거래 취소
    DISPUTED      // 분쟁 발생
}