package com.back.baton.domain.trade.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradeGroupTest {

    @Test
    @DisplayName("TradeGroup 생성 시 전달된 필드가 올바르게 설정된다 - SWAP")
    void create_swapFields() {
        TradeGroup group = TradeGroup.create(42L, TradeType.SWAP);

        assertThat(group.getMatchProposalId()).isEqualTo(42L);
        assertThat(group.getTradeType()).isEqualTo(TradeType.SWAP);
    }

    @Test
    @DisplayName("TradeGroup 생성 시 전달된 필드가 올바르게 설정된다 - PURCHASE")
    void create_purchaseFields() {
        TradeGroup group = TradeGroup.create(10L, TradeType.PURCHASE);

        assertThat(group.getMatchProposalId()).isEqualTo(10L);
        assertThat(group.getTradeType()).isEqualTo(TradeType.PURCHASE);
    }
}