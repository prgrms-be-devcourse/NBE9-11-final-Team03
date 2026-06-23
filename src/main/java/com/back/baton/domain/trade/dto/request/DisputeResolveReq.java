package com.back.baton.domain.trade.dto.request;

import com.back.baton.domain.trade.entity.DisputeVerdict;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "분쟁 처리 요청 DTO")
public record DisputeResolveReq(
        @NotNull
        @Schema(description = "판정 결과 (BUYER_WIN: 구매자 승소 -> 환불, SELLER_WIN: 판매자 승소 -> 정산)", example = "BUYER_WIN")
        DisputeVerdict verdict
) {
}