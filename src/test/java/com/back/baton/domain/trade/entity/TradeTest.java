package com.back.baton.domain.trade.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradeTest {

    @Test
    @DisplayName("거래 생성 시 초기 상태는 REQUESTED이다")
    void create_status() {
        Trade trade = Trade.create(1L, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.REQUESTED);
    }

    @Test
    @DisplayName("거래 생성 시 전달된 필드가 올바르게 설정된다")
    void create_fields() {
        Trade trade = Trade.create(1L, 10L, 20L, 30L, 5000, TradeType.SWAP);

        assertThat(trade.getMatchId()).isEqualTo(1L);
        assertThat(trade.getTalentId()).isEqualTo(10L);
        assertThat(trade.getBuyerId()).isEqualTo(20L);
        assertThat(trade.getSellerId()).isEqualTo(30L);
        assertThat(trade.getCreditPrice()).isEqualTo(5000);
        assertThat(trade.getTradeType()).isEqualTo(TradeType.SWAP);
    }
}