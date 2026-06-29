package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 관리자 유저 상태 변경 요청 값.
@Schema(description = "관리자 유저 상태 변경 요청 DTO")
public record AdminUserStatusUpdateReq(
        @NotNull(message = "변경할 유저 상태는 필수입니다.")
        @Schema(description = "변경할 유저 상태. WITHDRAWN은 관리자 API에서 직접 변경할 수 없습니다.", example = "SUSPENDED")
        UserStatus status,

        @Size(max = 500, message = "상태 변경 사유는 500자 이하로 입력해 주세요.")
        @Schema(description = "상태 변경 사유. 관리자 조치 이력에 기록됩니다.", example = "부적절한 활동으로 인한 이용 정지")
        String reason
) {
}
