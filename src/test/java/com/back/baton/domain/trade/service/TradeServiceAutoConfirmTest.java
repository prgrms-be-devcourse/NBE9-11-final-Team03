package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TradeErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceAutoConfirmTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private CreditService creditService;

    @Test
    @DisplayName("autoConfirm - 단방향 거래가 COMPLETED로 변경되고 에스크로가 RELEASED로 변경 및 정산된다")
    void autoConfirm_success_purchase() {
        // given
        Long tradeId = 1L;
        Trade trade = Trade.create(1L, null, 10L, 2L, 3L, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade, "id", tradeId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        Escrow escrow = Escrow.createHeld(tradeId, 2L, 3L, 5000, 500, 4500, LocalDateTime.now().plusDays(7));

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));

        // when
        TradeRes result = tradeService.autoConfirm(tradeId);

        // then
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.RELEASED);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED);

        // 크레딧 정산 호출 여부 검증
        verify(creditService).settleEscrow(
                escrow.getPayerId(),
                escrow.getPayeeId(),
                escrow.getAmount(),
                escrow.getAmount(),
                tradeId
        );
    }

    @Test
    @DisplayName("autoConfirm 실패 - 거래 상태가 UNDER_REVIEW가 아니면 TRADE_NOT_UNDER_REVIEW 예외가 발생한다")
    void autoConfirm_fail_notUnderReview() {
        // given
        Long tradeId = 1L;
        Trade trade = Trade.create(1L, null, 10L, 2L, 3L, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade, "id", tradeId);
        // 기본 상태인 IN_PROGRESS 상태 유지

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));

        // when & then
        assertThatThrownBy(() -> tradeService.autoConfirm(tradeId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).settleEscrow(any(), any(), anyInt(), anyInt(), any());
    }
}