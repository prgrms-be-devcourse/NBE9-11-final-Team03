package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.response.AdminActionLogRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.entity.AdminActionLog;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import com.back.baton.domain.admin.repository.AdminActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;

    @Transactional
    public void record(
            Long adminId,
            AdminActionTargetType targetType,
            Long targetId,
            AdminActionType actionType,
            String reason
    ) {
        adminActionLogRepository.save(AdminActionLog.create(adminId, targetType, targetId, actionType, reason));
    }

    public AdminPageRes<AdminActionLogRes> getActionLogs(
            Long adminId,
            AdminActionTargetType targetType,
            Long targetId,
            AdminActionType actionType,
            Pageable pageable
    ) {
        return AdminPageRes.from(adminActionLogRepository
                .searchAdminActionLogs(adminId, targetType, targetId, actionType, pageable)
                .map(AdminActionLogRes::from));
    }
}
