package com.back.baton.domain.escrow.entity;

public enum EscrowStatus {
    HELD,     // 크레딧 예치됨 (생성 시 초기 상태)
    RELEASED, // 거래 완료 후 판매자에게 정상 지급됨
    REFUNDED, // 거래 취소로 구매자에게 환불됨
    FROZEN    // 분쟁 발생으로 지급 보류됨
}