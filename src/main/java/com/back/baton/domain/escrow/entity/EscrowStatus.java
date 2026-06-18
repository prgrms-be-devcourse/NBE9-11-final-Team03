package com.back.baton.domain.escrow.entity;

public enum EscrowStatus {
    HELD,          // 크레딧 예치됨 (생성 시 초기 상태)
    IN_PROGRESS,   // 판매자 작업 진행 중
    IN_REVIEW,     // 구매자 검토 대기 중
    COMPLETED,     // 거래 완료 및 정산 완료
    CANCELLED,     // 거래 취소 및 환불 완료
    DISPUTED       // 분쟁 발생
}