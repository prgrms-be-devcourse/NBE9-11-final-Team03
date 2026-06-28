package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 거래 목록 조회 필터 조건.
@Schema(description = "관리자 거래 목록 조회 필터 요청 DTO")
public record AdminTradeSearchReq(
        @Schema(description = "거래 상태 필터. 생략하면 전체 상태를 조회합니다.", example = "IN_PROGRESS")
        TradeStatus status,

        @Schema(description = "구매자 ID 필터", example = "2")
        Long buyerId,

        @Schema(description = "판매자 ID 필터", example = "3")
        Long sellerId,

        @Schema(description = "거래 유형 필터. PURCHASE 또는 SWAP", example = "PURCHASE")
        TradeType tradeType
) {
}
