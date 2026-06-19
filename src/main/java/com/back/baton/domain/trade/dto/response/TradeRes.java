package com.back.baton.domain.trade.dto.response;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "거래 상태 조회 응답 DTO")
public record TradeRes(
        Long tradeId,
        Long matchId,
        Long talentId,
        Long buyerId,
        Long sellerId,
        Integer creditPrice,
        TradeType tradeType,
        TradeStatus tradeStatus,
        EscrowStatus escrowStatus,
        LocalDateTime escrowExpiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TradeRes of(Trade trade, Escrow escrow) {
        return new TradeRes(
                trade.getId(),
                trade.getMatchId(),
                trade.getTalentId(),
                trade.getBuyerId(),
                trade.getSellerId(),
                trade.getCreditPrice(),
                trade.getTradeType(),
                trade.getStatus(),
                escrow.getStatus(),
                escrow.getExpiresAt(),
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}