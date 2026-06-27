package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.DisputeRes;
import com.back.baton.domain.trade.dto.response.TradeListRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.*;
import com.back.baton.domain.trade.repository.TradeGroupRepository;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.global.response.CursorPageRes;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private CreditService creditService;

    @Mock
    private TradeGroupRepository tradeGroupRepository;

    @Test
    @DisplayName("단방향 거래 생성 시 IN_PROGRESS 상태로 저장된다")
    void createPurchaseTrade_savedWithInProgressStatus() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MatchProposal matchProposal = mock(MatchProposal.class);
        given(matchProposal.getId()).willReturn(1L);
        given(matchProposal.getProviderTalentId()).willReturn(10L);
        given(matchProposal.getRequesterId()).willReturn(20L);
        given(matchProposal.getProviderId()).willReturn(30L);
        given(matchProposal.getProviderTalentPriceSnapshot()).willReturn(5000);

        tradeService.createPurchaseTrade(matchProposal);

        then(tradeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("단방향 거래 생성 시 전달된 필드가 올바르게 저장된다")
    void createPurchaseTrade_savedWithCorrectFields() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MatchProposal matchProposal = mock(MatchProposal.class);
        given(matchProposal.getId()).willReturn(1L);
        given(matchProposal.getProviderTalentId()).willReturn(10L);
        given(matchProposal.getRequesterId()).willReturn(20L);
        given(matchProposal.getProviderId()).willReturn(30L);
        given(matchProposal.getProviderTalentPriceSnapshot()).willReturn(5000);

        tradeService.createPurchaseTrade(matchProposal);

        then(tradeRepository).should().save(captor.capture());
        Trade saved = captor.getValue();

        assertThat(saved.getMatchId()).isEqualTo(1L);
        assertThat(saved.getTradeGroupId()).isNull();
        assertThat(saved.getTalentId()).isEqualTo(10L);
        assertThat(saved.getBuyerId()).isEqualTo(20L);
        assertThat(saved.getSellerId()).isEqualTo(30L);
        assertThat(saved.getCreditPrice()).isEqualTo(5000);
        assertThat(saved.getTradeType()).isEqualTo(TradeType.PURCHASE);
    }

    @Test
    @DisplayName("양방향 교환 거래들이 올바르게 생성된다")
    void createSwapTrades_success() {
        given(tradeRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

        TradeGroup tradeGroup = TradeGroup.create(1L, TradeType.SWAP);

        ReflectionTestUtils.setField(tradeGroup, "id", 100L);
        MatchProposal matchProposal = mock(MatchProposal.class);

        given(matchProposal.getId()).willReturn(1L);
        given(matchProposal.getProviderTalentId()).willReturn(10L);
        given(matchProposal.getRequesterTalentId()).willReturn(99L);
        given(matchProposal.getRequesterId()).willReturn(20L);
        given(matchProposal.getProviderId()).willReturn(30L);
        given(matchProposal.getProviderTalentPriceSnapshot()).willReturn(5000);
        given(matchProposal.getRequesterTalentPriceSnapshot()).willReturn(3000);

        List<Trade> trades = tradeService.createSwapTrades(matchProposal, tradeGroup);

        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getMatchId()).isEqualTo(1L);
        assertThat(trades.get(0).getTradeGroupId()).isEqualTo(100L);
        assertThat(trades.get(0).getTradeType()).isEqualTo(TradeType.SWAP);
        assertThat(trades.get(1).getMatchId()).isEqualTo(1L);
        assertThat(trades.get(1).getTradeGroupId()).isEqualTo(100L);
        assertThat(trades.get(1).getTradeType()).isEqualTo(TradeType.SWAP);
    }

    @Test
    @DisplayName("구매자가 거래를 조회하면 거래와 에스크로 정보를 반환한다")
    void getTrade_buyer() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.getMyTrade(1L, buyerId);

        assertThat(result.tradeId()).isEqualTo(1L);
        assertThat(result.buyerId()).isEqualTo(buyerId);
        assertThat(result.sellerId()).isEqualTo(3L);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.HELD);
        assertThat(result.creditPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("판매자가 거래를 조회하면 거래와 에스크로 정보를 반환한다")
    void getMyTrade_seller() {
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        Escrow escrow = createEscrow(2L, sellerId);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.getMyTrade(1L, sellerId);

        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    @DisplayName("존재하지 않는 거래를 조회하면 TRADE_NOT_FOUND 예외가 발생한다")
    void getMyTrade_tradeNotFound() {
        given(tradeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.getMyTrade(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("에스크로가 없는 거래를 조회하면 ESCROW_NOT_FOUND 예외가 발생한다")
    void getMyTrade_escrowNotFound() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.getMyTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    @Test
    @DisplayName("거래 참여자가 아닌 사용자가 조회하면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void getMyTrade_accessDenied() {
        Long outsiderId = 999L;
        Trade trade = createTrade(2L, 3L);

        given(tradeRepository.findById(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.getMyTrade(1L, outsiderId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("구매자가 IN_PROGRESS 거래를 취소하면 CANCELLED 상태로 변경된다")
    void cancelTrade_buyerInProgress() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.cancelTrade(1L, buyerId);

        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
        verify(creditService).refundFromEscrow(escrow.getPayerId(), escrow.getAmount(), 1L);
    }

    @Test
    @DisplayName("판매자가 IN_PROGRESS 거래를 취소하면 CANCELLED 상태로 변경된다")
    void cancelTrade_sellerInProgress() {
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        Escrow escrow = createEscrow(2L, sellerId);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.cancelTrade(1L, sellerId);

        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
    }

    @Test
    @DisplayName("UNDER_REVIEW 상태의 거래는 취소할 수 없다")
    void cancelTrade_underReview() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_UNDER_REVIEW);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("존재하지 않는 거래를 취소하면 TRADE_NOT_FOUND 예외가 발생한다")
    void cancelTrade_tradeNotFound() {
        given(tradeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.cancelTrade(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("거래 참여자가 아닌 사용자가 취소하면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void cancelTrade_accessDenied() {
        Long outsiderId = 999L;
        Trade trade = createTrade(2L, 3L);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, outsiderId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("COMPLETED 상태의 거래는 취소할 수 없다")
    void cancelTrade_invalidStatus_completed() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.COMPLETED);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_COMPLETED);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("이미 취소된 거래는 다시 취소할 수 없다")
    void cancelTrade_invalidStatus_alreadyCancelled() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.CANCELLED);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_CANCELLED);

        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("분쟁 중인 거래는 취소할 수 없다")
    void cancelTrade_invalidStatus_disputed() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_IN_DISPUTE);

        verify(escrowRepository, never()).findByTradeId(any());
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("에스크로가 없는 거래를 취소하면 ESCROW_NOT_FOUND 예외가 발생한다")
    void cancelTrade_escrowNotFound() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.cancelTrade(1L, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);

        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("구매자가 UNDER_REVIEW 거래에 분쟁을 신청하면 DISPUTED/FROZEN 상태로 변경되고 사유가 저장된다")
    void disputeTrade_success() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.disputeTrade(1L, buyerId, "결과물이 약속한 조건과 다릅니다.");

        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.DISPUTED);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.FROZEN);
        assertThat(escrow.getRejectReason()).isEqualTo("결과물이 약속한 조건과 다릅니다.");
        assertThat(escrow.getExpiresAt()).isNull(); // 자동 확정 타이머 정지
        verify(creditService, never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("구매자가 아닌 사용자가 분쟁을 신청하면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void disputeTrade_notBuyer() {
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.disputeTrade(1L, sellerId, "사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("검토 중이 아닌 거래에 분쟁을 신청하면 TRADE_NOT_UNDER_REVIEW 예외가 발생한다")
    void disputeTrade_notUnderReview() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L); // IN_PROGRESS

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.disputeTrade(1L, buyerId, "사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("이미 분쟁 중인 거래에 다시 분쟁을 신청하면 TRADE_ALREADY_DISPUTED 예외가 발생한다")
    void disputeTrade_alreadyDisputed() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.disputeTrade(1L, buyerId, "사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_DISPUTED);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("존재하지 않는 거래에 분쟁을 신청하면 TRADE_NOT_FOUND 예외가 발생한다")
    void disputeTrade_tradeNotFound() {
        given(tradeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.disputeTrade(999L, 2L, "사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("에스크로가 없는 거래에 분쟁을 신청하면 ESCROW_NOT_FOUND 예외가 발생한다")
    void disputeTrade_escrowNotFound() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.disputeTrade(1L, buyerId, "사유"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    @Test
    @DisplayName("구매자 승소 시 거래가 CANCELLED, 에스크로가 REFUNDED 상태로 변경되고 환불이 실행된다")
    void resolveDispute_buyerWin() {
        Long buyerId = 2L;
        Long sellerId = 3L;
        Trade trade = createTrade(buyerId, sellerId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);
        Escrow escrow = createEscrow(buyerId, sellerId);
        ReflectionTestUtils.setField(escrow, "status", EscrowStatus.FROZEN);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.resolveDispute(1L, DisputeVerdict.BUYER_WIN);

        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
        verify(creditService).refundFromEscrow(escrow.getPayerId(), escrow.getAmount(), 1L);
        then(creditService).should(never()).settleEscrow(any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("판매자 승소 시 거래가 COMPLETED, 에스크로가 RELEASED 상태로 변경되고 정산이 실행된다")
    void resolveDispute_sellerWin() {
        Long buyerId = 2L;
        Long sellerId = 3L;
        Trade trade = createTrade(buyerId, sellerId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);
        Escrow escrow = createEscrow(buyerId, sellerId);
        ReflectionTestUtils.setField(escrow, "status", EscrowStatus.FROZEN);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.resolveDispute(1L, DisputeVerdict.SELLER_WIN);

        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
        verify(creditService).settleEscrow(escrow.getPayerId(), escrow.getPayeeId(), escrow.getAmount(), escrow.getSettlementAmount(), 1L);
        then(creditService).should(never()).refundFromEscrow(any(), anyInt(), any());
    }

    @Test
    @DisplayName("존재하지 않는 거래에 분쟁 처리하면 TRADE_NOT_FOUND 예외가 발생한다")
    void resolveDispute_tradeNotFound() {
        given(tradeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.resolveDispute(999L, DisputeVerdict.BUYER_WIN))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("DISPUTED 상태가 아닌 거래에 분쟁 처리하면 TRADE_NOT_DISPUTED 예외가 발생한다")
    void resolveDispute_tradeNotDisputed() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L); // IN_PROGRESS

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.resolveDispute(1L, DisputeVerdict.SELLER_WIN))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_DISPUTED);

        verify(escrowRepository, never()).findByTradeId(any());
    }

    @Test
    @DisplayName("에스크로가 없는 거래에 분쟁 처리하면 ESCROW_NOT_FOUND 예외가 발생한다")
    void resolveDispute_escrowNotFound() {
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);

        given(tradeRepository.findByIdWithLock(1L)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.resolveDispute(1L, DisputeVerdict.BUYER_WIN))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    @Test
    @DisplayName("분쟁 거래 목록을 정상 반환한다")
    void getDisputedTrades_success() {
        Trade trade = createTrade(2L, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.DISPUTED);
        Escrow escrow = createEscrow(2L, 3L);

        given(tradeRepository.findAllByStatus(TradeStatus.DISPUTED)).willReturn(List.of(trade));
        given(escrowRepository.findAllByTradeIdIn(any())).willReturn(List.of(escrow));

        List<DisputeRes> result = tradeService.getDisputedTrades();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tradeId()).isEqualTo(1L);
        assertThat(result.get(0).tradeStatus()).isEqualTo(TradeStatus.DISPUTED);
    }

    @Test
    @DisplayName("분쟁 거래가 없으면 빈 리스트를 반환하고 에스크로를 조회하지 않는다")
    void getDisputedTrades_empty() {
        given(tradeRepository.findAllByStatus(TradeStatus.DISPUTED)).willReturn(List.of());

        List<DisputeRes> result = tradeService.getDisputedTrades();

        assertThat(result).isEmpty();
        verify(escrowRepository, never()).findAllByTradeIdIn(any());
    }

    @Test
    @DisplayName("에스크로가 없는 분쟁 거래는 결과에서 제외된다")
    void getDisputedTrades_tradeWithoutEscrowFiltered() {
        Trade tradeWithEscrow = createTrade(2L, 3L);
        ReflectionTestUtils.setField(tradeWithEscrow, "status", TradeStatus.DISPUTED);

        Trade tradeWithoutEscrow = Trade.create(1L, null, 10L, 2L, 3L, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(tradeWithoutEscrow, "id", 2L);
        ReflectionTestUtils.setField(tradeWithoutEscrow, "status", TradeStatus.DISPUTED);

        Escrow escrow = createEscrow(2L, 3L);

        given(tradeRepository.findAllByStatus(TradeStatus.DISPUTED)).willReturn(List.of(tradeWithEscrow, tradeWithoutEscrow));
        given(escrowRepository.findAllByTradeIdIn(any())).willReturn(List.of(escrow));

        List<DisputeRes> result = tradeService.getDisputedTrades();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tradeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("status 필터 없이 조회하면 내 모든 거래 목록을 반환한다")
    void getMyTrades_noFilter() {
        Long userId = 2L;
        List<TradeListRes> rows = List.of(
                createTradeListRes(1L, TradeStatus.IN_PROGRESS, userId, 3L),
                createTradeListRes(2L, TradeStatus.COMPLETED, userId, 3L)
        );
        given(tradeRepository.findMyTrades(userId, null, null, 20)).willReturn(rows);

        CursorPageRes<TradeListRes> result = tradeService.getMyTrades(userId, null, null, 20);

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("status 필터를 적용하면 해당 상태의 거래만 반환한다")
    void getMyTrades_filteredByStatus() {
        Long userId = 2L;
        List<TradeListRes> rows = List.of(
                createTradeListRes(1L, TradeStatus.IN_PROGRESS, userId, 3L)
        );
        given(tradeRepository.findMyTrades(userId, TradeStatus.IN_PROGRESS, null, 20)).willReturn(rows);

        CursorPageRes<TradeListRes> result = tradeService.getMyTrades(userId, TradeStatus.IN_PROGRESS, null, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).tradeStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("거래가 없으면 빈 목록과 hasNext=false를 반환한다")
    void getMyTrades_emptyResult() {
        Long userId = 2L;
        given(tradeRepository.findMyTrades(userId, null, null, 20)).willReturn(List.of());

        CursorPageRes<TradeListRes> result = tradeService.getMyTrades(userId, null, null, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("다음 페이지가 있으면 hasNext=true이고 nextCursor가 마지막 항목의 tradeId다")
    void getMyTrades_hasNextTrue() {
        Long userId = 2L;
        int size = 2;
        List<TradeListRes> rows = List.of(
                createTradeListRes(3L, TradeStatus.IN_PROGRESS, userId, 3L),
                createTradeListRes(2L, TradeStatus.IN_PROGRESS, userId, 3L),
                createTradeListRes(1L, TradeStatus.IN_PROGRESS, userId, 3L) // size+1 번째
        );
        given(tradeRepository.findMyTrades(userId, null, null, size)).willReturn(rows);

        CursorPageRes<TradeListRes> result = tradeService.getMyTrades(userId, null, null, size);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(size);
        assertThat(result.nextCursor()).isEqualTo(2L); // 마지막으로 반환된 항목의 tradeId
    }

    @Test
    @DisplayName("cursor를 전달하면 해당 cursor 이후의 거래 목록을 반환한다")
    void getMyTrades_withCursor() {
        Long userId = 2L;
        Long cursor = 5L;
        List<TradeListRes> rows = List.of(
                createTradeListRes(4L, TradeStatus.IN_PROGRESS, userId, 3L),
                createTradeListRes(3L, TradeStatus.COMPLETED, userId, 3L)
        );
        given(tradeRepository.findMyTrades(userId, null, cursor, 20)).willReturn(rows);

        CursorPageRes<TradeListRes> result = tradeService.getMyTrades(userId, null, cursor, 20);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).tradeId()).isEqualTo(4L);
        assertThat(result.nextCursor()).isEqualTo(3L);
    }

    @Test
    @DisplayName("구매 확정 성공 - Trade가 COMPLETED, Escrow가 RELEASED로 변경된다")
    void confirmPurchase_success() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));

        TradeRes result = tradeService.confirmPurchase(tradeId, buyerId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED);
    }

    @Test
    @DisplayName("구매 확정 - 존재하지 않는 거래이면 TRADE_NOT_FOUND 예외가 발생한다")
    void confirmPurchase_tradeNotFound() {
        given(tradeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.confirmPurchase(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);
    }

    @Test
    @DisplayName("구매 확정 - 구매자가 아니면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void confirmPurchase_accessDenied() {
        Long tradeId = 1L;
        Trade trade = createTrade(2L, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.confirmPurchase(tradeId, 999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("구매 확정 - UNDER_REVIEW가 아닌 거래이면 TRADE_NOT_UNDER_REVIEW 예외가 발생한다")
    void confirmPurchase_notUnderReview() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.confirmPurchase(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("구매 확정 - 에스크로가 없으면 ESCROW_NOT_FOUND 예외가 발생한다")
    void confirmPurchase_escrowNotFound() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.confirmPurchase(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    @Test
    @DisplayName("양방향 교환 거래 생성 시 두 거래의 방향이 올바르게 교차 매핑된다")
    void createSwapTrades_directionVerification() {
        given(tradeRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

        TradeGroup tradeGroup = TradeGroup.create(1L, TradeType.SWAP);
        ReflectionTestUtils.setField(tradeGroup, "id", 100L);

        MatchProposal matchProposal = mock(MatchProposal.class);
        given(matchProposal.getId()).willReturn(1L);
        given(matchProposal.getProviderTalentId()).willReturn(10L);
        given(matchProposal.getRequesterTalentId()).willReturn(99L);
        given(matchProposal.getRequesterId()).willReturn(20L);
        given(matchProposal.getProviderId()).willReturn(30L);
        given(matchProposal.getProviderTalentPriceSnapshot()).willReturn(5000);
        given(matchProposal.getRequesterTalentPriceSnapshot()).willReturn(3000);

        List<Trade> trades = tradeService.createSwapTrades(matchProposal, tradeGroup);

        assertThat(trades).hasSize(2);

        Trade requesterTrade = trades.get(0);
        assertThat(requesterTrade.getBuyerId()).isEqualTo(20L);
        assertThat(requesterTrade.getSellerId()).isEqualTo(30L);
        assertThat(requesterTrade.getTalentId()).isEqualTo(10L);
        assertThat(requesterTrade.getCreditPrice()).isEqualTo(5000);

        Trade providerTrade = trades.get(1);
        assertThat(providerTrade.getBuyerId()).isEqualTo(30L);
        assertThat(providerTrade.getSellerId()).isEqualTo(20L);
        assertThat(providerTrade.getTalentId()).isEqualTo(99L);
        assertThat(providerTrade.getCreditPrice()).isEqualTo(3000);
    }

    private TradeListRes createTradeListRes(Long tradeId, TradeStatus status, Long buyerId, Long sellerId) {
        return new TradeListRes(tradeId, 10L, buyerId, sellerId, 5000, TradeType.PURCHASE, status,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Trade createTrade(Long buyerId, Long sellerId) {
        Trade trade = Trade.create(1L, null, 10L, buyerId, sellerId, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade, "id", 1L);
        return trade;
    }

    private Escrow createEscrow(Long payerId, Long payeeId) {
        return Escrow.createHeld(1L, payerId, payeeId, 5000, 500, 4500, LocalDateTime.now().plusDays(7));
    }
}