package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 에스크로 상세 응답")
public record AdminEscrowRes(
        @Schema(description = "에스크로 ID", example = "1")
        Long escrowId,

        @Schema(description = "거래 ID", example = "1")
        Long tradeId,

        @Schema(description = "지불자 ID", example = "2")
        Long payerId,

        @Schema(description = "수령자 ID", example = "3")
        Long payeeId,

        @Schema(description = "에스크로 예치 금액", example = "100")
        Integer amount,

        @Schema(description = "수수료", example = "10")
        Integer fee,

        @Schema(description = "정산 예정 금액", example = "90")
        Integer settlementAmount,

        @Schema(description = "에스크로 상태", example = "HELD")
        EscrowStatus status,

        @Schema(description = "동결/반려 사유", example = "분쟁 발생")
        String rejectReason,

        LocalDateTime expiresAt,
        LocalDateTime settledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminEscrowRes from(Escrow escrow) {
        return new AdminEscrowRes(
                escrow.getId(),
                escrow.getTradeId(),
                escrow.getPayerId(),
                escrow.getPayeeId(),
                escrow.getAmount(),
                escrow.getFee(),
                escrow.getSettlementAmount(),
                escrow.getStatus(),
                escrow.getRejectReason(),
                escrow.getExpiresAt(),
                escrow.getSettledAt(),
                escrow.getCreatedAt(),
                escrow.getUpdatedAt()
        );
    }
}
