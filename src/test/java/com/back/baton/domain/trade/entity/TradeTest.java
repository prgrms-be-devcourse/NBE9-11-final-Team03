package com.back.baton.domain.trade.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradeTest {

    @Test
    @DisplayName("거래 생성 시 초기 상태는 IN_PROGRESS이다")
    void create_status() {
        Trade trade = Trade.create(1L, null, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("거래 생성 시 전달된 필드가 올바르게 설정된다")
    void create_fields() {
        Trade trade = Trade.create(1L, 100L, 10L, 20L, 30L, 5000, TradeType.SWAP);

        assertThat(trade.getMatchId()).isEqualTo(1L);
        assertThat(trade.getTradeGroupId()).isEqualTo(100L);
        assertThat(trade.getTalentId()).isEqualTo(10L);
        assertThat(trade.getBuyerId()).isEqualTo(20L);
        assertThat(trade.getSellerId()).isEqualTo(30L);
        assertThat(trade.getCreditPrice()).isEqualTo(5000);
        assertThat(trade.getTradeType()).isEqualTo(TradeType.SWAP);
    }

    @Test
    @DisplayName("거래 취소 시 상태가 CANCELLED로 변경된다")
    void cancel_status() {
        Trade trade = Trade.create(1L, null, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        trade.cancel();

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
    }

    @Test
    @DisplayName("분쟁 신청 시 상태가 DISPUTED로 변경된다")
    void dispute_status() {
        // 거래 생성 (초기 상태: IN_PROGRESS)
        Trade trade = Trade.create(1L, null, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        // 결과물 제출 처리 추가 (상태: IN_PROGRESS -> UNDER_REVIEW)
        trade.submitResult();

        // 분쟁 신청 진행 (상태: UNDER_REVIEW -> DISPUTED)
        trade.dispute();

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.DISPUTED);
    }

    @Test
    @DisplayName("결과물 제출 시 상태가 UNDER_REVIEW로 변경된다")
    void submitResult_status() {
        Trade trade = Trade.create(1L, null, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        trade.submitResult();

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("거래 완료 시 상태가 COMPLETED로 변경된다")
    void complete_status() {
        Trade trade = Trade.create(1L, null, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        trade.complete();

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
    }
}