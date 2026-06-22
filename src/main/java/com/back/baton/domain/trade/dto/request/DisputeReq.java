package com.back.baton.domain.trade.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "분쟁 신청 요청 DTO")
public record DisputeReq(
        @NotBlank
        @Size(max = 200)
        @Schema(description = "분쟁 사유", example = "결과물이 최초 약속한 조건과 다릅니다.")
        String reason
) {
}