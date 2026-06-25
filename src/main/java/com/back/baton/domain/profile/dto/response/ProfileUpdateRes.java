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
        List<ProfileCategoryRes> myTalentCategories,
        List<ProfileCategoryRes> wantTalentCategories,
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
                List.copyOf(profile.getPortfolioLinkList()),
                getCategoryResList(profile.getMyTalentCategories()),
                getCategoryResList(profile.getWantTalentCategories()),
                profile.isVisible(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
    private static List<ProfileCategoryRes> getCategoryResList(List<Category> list){
        return list.stream().map(ProfileCategoryRes::new).toList();
    }
}
