package com.back.baton.domain.trade.dto.response;

import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "거래 목록 조회 응답 DTO")
public record TradeListRes(
        Long tradeId,
        Long talentId,
        Long buyerId,
        Long sellerId,
        Integer creditPrice,
        TradeType tradeType,
        TradeStatus tradeStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}