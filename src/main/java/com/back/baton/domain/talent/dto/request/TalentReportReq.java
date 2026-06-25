package com.back.baton.domain.talent.dto.request;

import com.back.baton.domain.talent.entity.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "재능 신고 요청 DTO")
public record TalentReportReq(
        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,

        @Schema(description = "신고 상세 설명", example = "외부 연락처로 거래를 유도합니다.", maxLength = 1000)
        @Size(max = 1000, message = "상세 설명은 1000자 이하여야 합니다.")
        String description
) {}