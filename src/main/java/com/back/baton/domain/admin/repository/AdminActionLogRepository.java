package com.back.baton.domain.admin.repository;

import com.back.baton.domain.admin.entity.AdminActionLog;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    @Query("""
            SELECT l
            FROM AdminActionLog l
            WHERE (:adminId IS NULL OR l.adminId = :adminId)
              AND (:targetType IS NULL OR l.targetType = :targetType)
              AND (:targetId IS NULL OR l.targetId = :targetId)
              AND (:actionType IS NULL OR l.actionType = :actionType)
            """)
    Page<AdminActionLog> searchAdminActionLogs(
            @Param("adminId") Long adminId,
            @Param("targetType") AdminActionTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("actionType") AdminActionType actionType,
            Pageable pageable
    );
}
