package com.back.baton.domain.escrow.entity;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscrowTest {

    @Test
    @DisplayName("에스크로 생성 시 초기 상태는 HELD이다")
    void createHeld_status() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    @DisplayName("에스크로 생성 시 fee는 0이고 settlementAmount는 amount와 같다")
    void createHeld_feeAndSettlement() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        assertThat(escrow.getFee()).isEqualTo(500);
        assertThat(escrow.getSettlementAmount()).isEqualTo(4500);
    }

    @Test
    @DisplayName("에스크로 생성 시 전달된 필드가 올바르게 설정된다")
    void createHeld_fields() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, expiresAt);

        assertThat(escrow.getTradeId()).isEqualTo(1L);
        assertThat(escrow.getPayerId()).isEqualTo(10L);
        assertThat(escrow.getPayeeId()).isEqualTo(20L);
        assertThat(escrow.getAmount()).isEqualTo(5000);
        assertThat(escrow.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("에스크로 환불 시 상태가 REFUNDED로 변경된다")
    void refund_status() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        escrow.refund();

        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.REFUNDED);
    }

    @Test
    @DisplayName("에스크로 환불 시 settledAt이 설정된다")
    void refund_settledAt() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        escrow.refund();

        assertThat(escrow.getSettledAt()).isNotNull();
    }

    @Test
    @DisplayName("HELD 상태가 아닌 에스크로를 환불하면 INVALID_ESCROW_STATUS 예외가 발생한다")
    void refund_invalidStatus_refunded() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(escrow, "status", EscrowStatus.REFUNDED);

        assertThatThrownBy(escrow::refund)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.INVALID_ESCROW_STATUS);
    }

    @Test
    @DisplayName("RELEASED 상태의 에스크로를 환불하면 INVALID_ESCROW_STATUS 예외가 발생한다")
    void refund_invalidStatus_released() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(escrow, "status", EscrowStatus.RELEASED);

        assertThatThrownBy(escrow::refund)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.INVALID_ESCROW_STATUS);
    }

    @Test
    @DisplayName("에스크로 동결 시 FROZEN 상태로 변경되고 사유 저장 및 만료 시각이 초기화된다")
    void freeze_status() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        escrow.freeze("결과물이 약속한 조건과 다릅니다.");

        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.FROZEN);
        assertThat(escrow.getRejectReason()).isEqualTo("결과물이 약속한 조건과 다릅니다.");
        assertThat(escrow.getExpiresAt()).isNull(); // 자동 확정 타이머 정지
    }

    @Test
    @DisplayName("HELD 상태가 아닌 에스크로를 동결하면 INVALID_ESCROW_STATUS 예외가 발생한다")
    void freeze_invalidStatus() {
        Escrow escrow = Escrow.createHeld(1L, 10L, 20L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(escrow, "status", EscrowStatus.FROZEN);

        assertThatThrownBy(() -> escrow.freeze("사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.INVALID_ESCROW_STATUS);
    }
}