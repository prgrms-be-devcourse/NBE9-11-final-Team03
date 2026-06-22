package com.back.baton.domain.profile.dto.response;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.profile.entity.Profile;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileUpdateRes(
        Long id,
        String nickname,
        String profileImageUrl,
        String introduction,
        List<String> portfolioLinkList,
        List<Category> myTalentCategories,
        List<Category> wantTalentCategories,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ProfileUpdateRes(Profile profile) {
        this(
                profile.getId(),
                profile.getUser().getNickname(),
                profile.getUser().getProfileImageUrl(),
                profile.getUser().getIntroduction(),
                profile.getPortfolioLinkList(),
                profile.getMyTalentCategories(),
                profile.getWantTalentCategories(),
                profile.isVisible(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
