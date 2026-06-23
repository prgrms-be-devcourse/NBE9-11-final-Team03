package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final EscrowRepository escrowRepository;
    private final CreditService creditService;

    @Transactional
    public Trade create(Long matchId, Long talentId, Long buyerId, Long sellerId, Integer creditPrice, TradeType tradeType) {
        Trade trade = Trade.create(matchId, talentId, buyerId, sellerId, creditPrice, tradeType);
        return tradeRepository.save(trade);
    }

    public TradeRes getTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateTradeParticipant(trade, userId);

        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        return TradeRes.of(trade, escrow);
    }

    @Transactional
    public TradeRes cancelTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateTradeParticipant(trade, userId);
        validateCancellable(trade);

        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        trade.cancel();
        escrow.refund();

        creditService.refundFromEscrow(
                escrow.getPayerId(),
                escrow.getAmount(),
                tradeId,
                "TRADE-CANCEL-" + tradeId
        );

        return TradeRes.of(trade, escrow);
    }

    @Transactional
    public TradeRes disputeTrade(Long tradeId, Long buyerId, String reason) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateBuyer(trade, buyerId);
        validateDisputable(trade);

        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        trade.dispute(); // 거래 상태 변경 (UNDER_REVIEW -> DISPUTED)
        escrow.freeze(reason); // 에스크로 상태 변경 (HELD -> FROZEN)

        return TradeRes.of(trade, escrow);
    }

    private void validateTradeParticipant(Trade trade, Long userId) {
        if (!Objects.equals(trade.getBuyerId(), userId) && !Objects.equals(trade.getSellerId(), userId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateCancellable(Trade trade) {
        switch (trade.getStatus()) {
            case UNDER_REVIEW -> throw new CustomException(TradeErrorCode.TRADE_UNDER_REVIEW);
            case COMPLETED -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_COMPLETED);
            case CANCELLED -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_CANCELLED);
            case DISPUTED -> throw new CustomException(TradeErrorCode.TRADE_IN_DISPUTE);
            default -> {} // IN_PROGRESS 만 취소 가능
        }
    }

    private void validateBuyer(Trade trade, Long buyerId) {
        if (!Objects.equals(trade.getBuyerId(), buyerId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateDisputable(Trade trade) {
        switch (trade.getStatus()) {
            case UNDER_REVIEW -> {} // 검토 중인 거래만 분쟁 신청 가능
            case DISPUTED -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_DISPUTED);
            default -> throw new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);
        }
    }
}