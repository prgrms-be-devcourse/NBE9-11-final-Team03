package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.admin.entity.AdminActionLog;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import java.time.LocalDateTime;

public record AdminActionLogRes(
        Long logId,
        Long adminId,
        AdminActionTargetType targetType,
        Long targetId,
        AdminActionType actionType,
        String reason,
        LocalDateTime createdAt
) {
    public static AdminActionLogRes from(AdminActionLog log) {
        return new AdminActionLogRes(
                log.getId(),
                log.getAdminId(),
                log.getTargetType(),
                log.getTargetId(),
                log.getActionType(),
                log.getReason(),
                log.getCreatedAt()
        );
    }
}
