package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.TalentReport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "재능 신고 응답 DTO")
public record TalentReportRes(
        @Schema(description = "신고 ID", example = "10")
        Long reportId,
        @Schema(description = "신고 대상 재능 ID", example = "1")
        Long talentId,
        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT")
        ReportReason reason,
        @Schema(description = "신고 처리 상태", example = "PENDING")
        ReportStatus status,
        LocalDateTime createdAt
) {
    public static TalentReportRes from(TalentReport report) {
        return new TalentReportRes(
                report.getId(),
                report.getTalent().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}