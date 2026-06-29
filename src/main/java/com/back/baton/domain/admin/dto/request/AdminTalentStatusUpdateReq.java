package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.talent.entity.TalentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 관리자 재능 상태 변경 요청 값.
@Schema(description = "관리자 재능 상태 변경 요청 DTO")
public record AdminTalentStatusUpdateReq(
        @NotNull(message = "변경할 재능 상태는 필수입니다.")
        @Schema(description = "변경할 재능 상태", example = "CLOSED")
        TalentStatus status,

        @Size(max = 500, message = "상태 변경 사유는 500자 이하로 입력해 주세요.")
        @Schema(description = "상태 변경 사유. 관리자 조치 이력에 기록됩니다.", example = "신고 누적으로 인한 재능 비공개 처리")
        String reason
) {
}
