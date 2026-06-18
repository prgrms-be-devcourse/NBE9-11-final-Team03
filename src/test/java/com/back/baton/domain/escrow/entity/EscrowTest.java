package com.back.baton.domain.escrow.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EscrowTest {

    @Test
    @DisplayName("에스크로 생성 시 초기 상태는 HELD이다")
    void createHeld_status() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, LocalDateTime.now().plusDays(7));

        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    @DisplayName("에스크로 생성 시 fee는 0이고 settlementAmount는 amount와 같다")
    void createHeld_feeAndSettlement() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, LocalDateTime.now().plusDays(7));

        assertThat(escrow.getFee()).isEqualTo(0);
        assertThat(escrow.getSettlementAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("에스크로 생성 시 전달된 필드가 올바르게 설정된다")
    void createHeld_fields() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, expiresAt);

        assertThat(escrow.getTradeId()).isEqualTo(1L);
        assertThat(escrow.getPayerId()).isEqualTo(10L);
        assertThat(escrow.getPayeeId()).isEqualTo(20L);
        assertThat(escrow.getAmount()).isEqualTo(5000);
        assertThat(escrow.getExpiresAt()).isEqualTo(expiresAt);
    }
}