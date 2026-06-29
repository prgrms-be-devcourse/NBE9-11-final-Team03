package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserRole;
import com.back.baton.domain.user.entity.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 관리자 유저 조회 응답.
public record AdminUserRes(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        String introduction,
        BigDecimal trustScore,
        UserStatus status,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserRes from(User user) {
        return new AdminUserRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getIntroduction(),
                user.getTrustScore(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
