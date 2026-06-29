package com.back.baton.domain.admin.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_action_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActionLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private AdminActionTargetType targetType;

    @Column(nullable = false, updatable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, updatable = false)
    private AdminActionType actionType;

    @Column(length = 500, updatable = false)
    private String reason;

    public static AdminActionLog create(
            Long adminId,
            AdminActionTargetType targetType,
            Long targetId,
            AdminActionType actionType,
            String reason
    ) {
        AdminActionLog log = new AdminActionLog();
        log.adminId = adminId;
        log.targetType = targetType;
        log.targetId = targetId;
        log.actionType = actionType;
        log.reason = reason;
        return log;
    }
}
