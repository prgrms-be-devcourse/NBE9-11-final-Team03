package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 조치 이력 조회 필터 요청 DTO")
public record AdminActionLogSearchReq(
        Long adminId,
        AdminActionTargetType targetType,
        Long targetId,
        AdminActionType actionType
) {
}
