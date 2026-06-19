package com.back.baton.domain.credit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditTransactionType {
    WELCOME("신규 가입 웰컴 크레딧 지급"),
    PURCHASE_DEBIT("상품 구매로 인한 크레딧 차감"),
    ESCROW_HOLD("거래 완료까지 크레딧 에스크로 예치"),
    ESCROW_RELEASE("거래 완료 후 판매자 크레딧 지급"),
    REFUND("거래 취소로 인한 크레딧 환불"),
    CHARGE("크레딧 유료 충전"),
    REFERRAL_REWARD("추천인 보상 크레딧 적립"),
    ADJUSTMENT("관리자 수동 크레딧 조정");

    private final String defaultReason;
}
