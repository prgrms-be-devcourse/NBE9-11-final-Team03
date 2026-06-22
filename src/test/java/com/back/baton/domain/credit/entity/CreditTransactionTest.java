package com.back.baton.domain.credit.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditTransactionTest {

    @Test
    @DisplayName("원장 생성 시 전달된 필드가 올바르게 설정된다")
    void create_fields() {
        CreditTransaction tx = CreditTransaction.create(
                1L, 10L, CreditTransactionType.ESCROW_HOLD, -5000, 5000, "key-001", "보상 처리"
        );

        assertThat(tx.getUserId()).isEqualTo(1L);
        assertThat(tx.getRelatedTradeId()).isEqualTo(10L);
        assertThat(tx.getType()).isEqualTo(CreditTransactionType.ESCROW_HOLD);
        assertThat(tx.getAmount()).isEqualTo(-5000);
        assertThat(tx.getBalanceAfter()).isEqualTo(5000);
        assertThat(tx.getIdempotencyKey()).isEqualTo("key-001");
        // default_reason 은 타입의 기본 사유 스냅샷, detail_reason 은 전달된 건별 사유
        assertThat(tx.getDefaultReason()).isEqualTo(CreditTransactionType.ESCROW_HOLD.getDefaultReason());
        assertThat(tx.getDetailReason()).isEqualTo("보상 처리");
    }

    @Test
    @DisplayName("거래와 무관한 원장은 relatedTradeId가 null이다")
    void create_withoutRelatedTrade() {
        CreditTransaction tx = CreditTransaction.create(
                1L, null, CreditTransactionType.WELCOME, 10000, 10000, "key-002", "웰컴 크레딧"
        );

        assertThat(tx.getRelatedTradeId()).isNull();
    }

    @Test
    @DisplayName("차감 원장의 amount는 음수이다")
    void create_debitAmountIsNegative() {
        CreditTransaction tx = CreditTransaction.create(
                1L, null, CreditTransactionType.PURCHASE_DEBIT, -3000, 7000, "key-003", null
        );

        assertThat(tx.getAmount()).isNegative();
    }
}