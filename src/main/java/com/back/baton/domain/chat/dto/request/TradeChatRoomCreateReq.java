package com.back.baton.domain.chat.dto.request;

import com.back.baton.domain.trade.entity.Trade;

public record TradeChatRoomCreateReq(
        Long tradeId,
        Long talentId,
        Long buyerId,
        Long sellerId
) {
    public static TradeChatRoomCreateReq from(Trade trade) {
        return new TradeChatRoomCreateReq(
                trade.getId(),
                trade.getTalentId(),
                trade.getBuyerId(),
                trade.getSellerId()
        );
    }
}