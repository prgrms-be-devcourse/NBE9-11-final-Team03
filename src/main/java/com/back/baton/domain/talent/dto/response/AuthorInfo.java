package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.user.entity.User;

import java.math.BigDecimal;

public record AuthorInfo(
        Long authorId,
        String nickname,
        String profileImageUrl,
        String introduction,
        BigDecimal trustScore
) {
    public static AuthorInfo from(User author) {
        return new AuthorInfo(
                author.getId(),
                author.getNickname(),
                author.getProfileImageUrl(),
                author.getIntroduction(),
                author.getTrustScore()
        );
    }
}