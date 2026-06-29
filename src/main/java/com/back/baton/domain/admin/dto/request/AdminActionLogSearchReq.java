package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "관리자 조치 이력 조회 필터 요청 DTO")
public record AdminActionLogSearchReq(
        @Positive(message = "관리자 ID는 양수여야 합니다.")
        Long adminId,
        AdminActionTargetType targetType,
        @Positive(message = "대상 ID는 양수여야 합니다.")
        Long targetId,
        AdminActionType actionType
) {
}
