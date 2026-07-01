package com.back.baton.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

// 관리자 신고 처리 완료 요청 값.
@Schema(description = "관리자 신고 처리 완료 요청 DTO")
public record AdminReportResolveReq(
        @Size(max = 500, message = "신고 처리 메모는 500자 이하로 입력해 주세요.")
        @Schema(description = "신고 처리 메모. 관리자 조치 이력에 기록됩니다.", example = "신고 내용 확인 후 처리 완료")
        String memo
) {
}
