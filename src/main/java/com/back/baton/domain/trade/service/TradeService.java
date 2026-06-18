package com.back.baton.domain.trade.service;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;

    @Transactional
    public Trade create(Long matchId, Long talentId, Long buyerId, Long sellerId, Integer creditPrice, TradeType tradeType) {
        Trade trade = Trade.create(matchId, talentId, buyerId, sellerId, creditPrice, tradeType);
        return tradeRepository.save(trade);
    }
}