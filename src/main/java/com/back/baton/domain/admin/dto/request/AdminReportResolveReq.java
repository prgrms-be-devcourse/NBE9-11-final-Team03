package com.back.baton.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 신고 처리 완료 요청 값.
@Schema(description = "관리자 신고 처리 완료 요청 DTO")
public record AdminReportResolveReq(
        @Schema(description = "신고 처리 메모. 관리자 조치 이력에 기록됩니다.", example = "신고 내용 확인 후 처리 완료")
        String memo
) {
}
