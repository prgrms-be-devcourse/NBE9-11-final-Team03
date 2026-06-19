package com.back.baton.domain.trade.service;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
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
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        return TradeRes.of(trade, escrow);
    }

    private void validateTradeParticipant(Trade trade, Long userId) {
        if (!Objects.equals(trade.getBuyerId(), userId) && !Objects.equals(trade.getSellerId(), userId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }
}