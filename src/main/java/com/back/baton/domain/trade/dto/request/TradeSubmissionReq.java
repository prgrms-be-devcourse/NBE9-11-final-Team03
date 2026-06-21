package com.back.baton.domain.trade.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "결과물 제출 요청 DTO")
public record TradeSubmissionReq(
        @NotBlank
        @Size(max = 200)
        @Schema(description = "S3에 업로드된 파일 키", example = "trades/1/uuid.pdf")
        String fileKey,

        @Size(max = 200)
        @Schema(description = "결과물 설명")
        String description
) {
}