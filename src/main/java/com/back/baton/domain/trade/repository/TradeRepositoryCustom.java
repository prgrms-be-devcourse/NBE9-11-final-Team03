package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.dto.response.TradeListRes;
import com.back.baton.domain.trade.entity.TradeStatus;

import java.util.List;

public interface TradeRepositoryCustom {

    List<TradeListRes> findMyTrades(Long userId, TradeStatus status, Long cursor, int size);
}
