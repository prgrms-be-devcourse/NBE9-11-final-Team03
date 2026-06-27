package com.back.baton.domain.trade.service;

import com.back.baton.domain.trade.entity.TradeGroup;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeGroupService {

    private final TradeGroupRepository tradeGroupRepository;

    @Transactional
    public TradeGroup create(Long matchProposalId, TradeType tradeType) {
        return tradeGroupRepository.saveAndFlush(TradeGroup.create(matchProposalId, tradeType));
    }
}