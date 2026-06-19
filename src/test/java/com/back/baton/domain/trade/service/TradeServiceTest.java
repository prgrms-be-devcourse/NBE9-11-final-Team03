package com.back.baton.domain.trade.service;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @Test
    @DisplayName("거래 생성 시 IN_PROGRESS 상태로 저장된다")
    void create_savedWithInProgressStatus() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        tradeService.create(1L, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        then(tradeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("거래 생성 시 전달된 필드가 올바르게 저장된다")
    void create_savedWithCorrectFields() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        tradeService.create(1L, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        then(tradeRepository).should().save(captor.capture());
        Trade saved = captor.getValue();
        assertThat(saved.getMatchId()).isEqualTo(1L);
        assertThat(saved.getTalentId()).isEqualTo(10L);
        assertThat(saved.getBuyerId()).isEqualTo(20L);
        assertThat(saved.getSellerId()).isEqualTo(30L);
        assertThat(saved.getCreditPrice()).isEqualTo(5000);
        assertThat(saved.getTradeType()).isEqualTo(TradeType.PURCHASE);
    }

    @Test
    @DisplayName("구매자가 거래를 조회하면 거래와 에스크로 정보를 반환한다")
    void getTrade_buyer() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        Escrow escrow = createEscrow(1L, buyerId, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.getTrade(1L, buyerId);

        assertThat(result.tradeId()).isEqualTo(1L);
        assertThat(result.buyerId()).isEqualTo(buyerId);
        assertThat(result.sellerId()).isEqualTo(3L);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.HELD);
        assertThat(result.creditPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("판매자가 거래를 조회하면 거래와 에스크로 정보를 반환한다")
    void getTrade_seller() {
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        Escrow escrow = createEscrow(1L, 2L, sellerId);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.getTrade(1L, sellerId);

        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    @DisplayName("존재하지 않는 거래를 조회하면 TRADE_NOT_FOUND 예외가 발생한다")
    void getTrade_tradeNotFound() {
        given(tradeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.getTrade(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("에스크로가 없는 거래를 조회하면 ESCROW_NOT_FOUND 예외가 발생한다")
    void getTrade_escrowNotFound() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.getTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    @Test
    @DisplayName("거래 참여자가 아닌 사용자가 조회하면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void getTrade_accessDenied() {
        Long outsiderId = 999L;
        Trade trade = createTrade(2L, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.getTrade(1L, outsiderId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    private Trade createTrade(Long buyerId, Long sellerId) {
        Trade trade = Trade.create(1L, 10L, buyerId, sellerId, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade, "id", 1L);
        return trade;
    }

    private Escrow createEscrow(Long tradeId, Long payerId, Long payeeId) {
        return Escrow.createHeld(tradeId, payerId, payeeId, 5000, LocalDateTime.now().plusDays(7));
    }
}