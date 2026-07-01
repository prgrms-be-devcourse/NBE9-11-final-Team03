package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 신고 목록 조회 필터 조건.
@Schema(description = "관리자 신고 목록 조회 필터 요청 DTO")
public record AdminReportSearchReq(
        @Schema(description = "신고 처리 상태 필터. 생략하면 전체 상태를 조회합니다.", example = "PENDING")
        ReportStatus status,

        @Schema(description = "신고 사유 필터", example = "INAPPROPRIATE_CONTENT")
        ReportReason reason
) {
}
