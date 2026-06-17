package com.back.baton.domain.user.dto.response;

import com.back.baton.domain.user.entity.UserRole;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserSignupRes(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String introduction,
        UserStatus status,
        UserRole role,  //추가함
        LocalDateTime createdAt
) {
    public UserSignupRes(User user) {
        this(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getIntroduction(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
