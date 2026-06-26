package com.back.baton.domain.trade.dto.response;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "분쟁 거래 조회 응답 DTO")
public record DisputeRes(
        Long tradeId,
        Long tradeGroupId,
        Long talentId,
        Long buyerId,
        Long sellerId,
        Integer creditPrice,
        TradeType tradeType,
        TradeStatus tradeStatus,
        EscrowStatus escrowStatus,
        String disputeReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DisputeRes of(Trade trade, Escrow escrow) {
        return new DisputeRes(
                trade.getId(),
                trade.getTradeGroupId(),
                trade.getTalentId(),
                trade.getBuyerId(),
                trade.getSellerId(),
                trade.getCreditPrice(),
                trade.getTradeType(),
                trade.getStatus(),
                escrow.getStatus(),
                escrow.getRejectReason(),
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}