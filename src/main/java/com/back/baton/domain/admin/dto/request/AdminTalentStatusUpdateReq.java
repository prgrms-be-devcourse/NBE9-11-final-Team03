package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.talent.entity.TalentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 재능 상태 변경 요청 값.
@Schema(description = "관리자 재능 상태 변경 요청 DTO")
public record AdminTalentStatusUpdateReq(
        @Schema(description = "변경할 재능 상태", example = "CLOSED")
        TalentStatus status,

        @Schema(description = "상태 변경 사유. 관리자 조치 이력에 기록됩니다.", example = "신고 누적으로 인한 재능 비공개 처리")
        String reason
) {
}
