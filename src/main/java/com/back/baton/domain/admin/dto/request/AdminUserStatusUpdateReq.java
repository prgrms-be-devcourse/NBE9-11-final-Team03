package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 유저 상태 변경 요청 값.
@Schema(description = "관리자 유저 상태 변경 요청 DTO")
public record AdminUserStatusUpdateReq(
        @Schema(description = "변경할 유저 상태. WITHDRAWN은 관리자 API에서 직접 변경할 수 없습니다.", example = "SUSPENDED")
        UserStatus status,

        @Schema(description = "상태 변경 사유. 관리자 조치 이력에 기록됩니다.", example = "부적절한 활동으로 인한 이용 정지")
        String reason
) {
}
