package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.TalentReport;
import java.time.LocalDateTime;

// 관리자 신고 조회 응답.
public record AdminTalentReportRes(
        Long reportId,
        Long talentId,
        Long reporterId,
        ReportReason reason,
        String description,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminTalentReportRes from(TalentReport report) {
        return new AdminTalentReportRes(
                report.getId(),
                report.getTalent().getId(),
                report.getReporterId(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
