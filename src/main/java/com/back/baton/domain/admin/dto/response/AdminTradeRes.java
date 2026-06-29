package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 거래 목록/요약 응답")
public record AdminTradeRes(
        @Schema(description = "거래 ID", example = "1")
        Long tradeId,

        @Schema(description = "매칭 제안 ID", example = "10")
        Long matchId,

        @Schema(description = "양방향 거래 그룹 ID. PURCHASE 거래는 null일 수 있습니다.", example = "100")
        Long tradeGroupId,

        @Schema(description = "재능 ID", example = "3")
        Long talentId,

        @Schema(description = "구매자 ID", example = "2")
        Long buyerId,

        @Schema(description = "판매자 ID", example = "4")
        Long sellerId,

        @Schema(description = "거래 크레딧 가격", example = "100")
        Integer creditPrice,

        @Schema(description = "거래 유형", example = "PURCHASE")
        TradeType tradeType,

        @Schema(description = "거래 상태", example = "IN_PROGRESS")
        TradeStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminTradeRes from(Trade trade) {
        return new AdminTradeRes(
                trade.getId(),
                trade.getMatchId(),
                trade.getTradeGroupId(),
                trade.getTalentId(),
                trade.getBuyerId(),
                trade.getSellerId(),
                trade.getCreditPrice(),
                trade.getTradeType(),
                trade.getStatus(),
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}
