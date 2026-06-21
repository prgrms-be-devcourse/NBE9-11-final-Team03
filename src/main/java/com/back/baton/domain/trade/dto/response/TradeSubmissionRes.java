package com.back.baton.domain.trade.dto.response;

import com.back.baton.domain.trade.entity.TradeSubmission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "결과물 제출 응답 DTO")
public record TradeSubmissionRes(
        @Schema(description = "증빙 ID")
        Long id,

        @Schema(description = "에스크로 ID")
        Long escrowId,

        @Schema(description = "결과물 조회용 Presigned GET URL") // 판매자 자신의 결과물 확인용
        String fileUrl,

        @Schema(description = "결과물 설명")
        String description,

        @Schema(description = "제출 시각")
        LocalDateTime submittedAt
) {
    public static TradeSubmissionRes of(TradeSubmission submission, String fileUrl) {
        return new TradeSubmissionRes(
                submission.getId(),
                submission.getEscrowId(),
                fileUrl,
                submission.getDescription(),
                submission.getSubmittedAt()
        );
    }
}