package com.back.baton.domain.profile.dto.response;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.profile.entity.Profile;

import java.math.BigDecimal;
import java.util.List;

public record MyProfileDetailRes(
        Long id,
        String nickname,
        String profileImageUrl,
        String introduction,
        BigDecimal trustScore,
        List<String> portfolioLinkList,
        List<ProfileCategoryRes> myTalentCategories,
        List<ProfileCategoryRes> wantTalentCategories,
        boolean visible
) {

    public MyProfileDetailRes(Profile profile) {
        this(
                profile.getId(),
                profile.getUser().getNickname(),
                profile.getUser().getProfileImageUrl(),
                profile.getUser().getIntroduction(),
                profile.getUser().getTrustScore(),
                profile.getPortfolioLinkList(),
                getCategoryResList(profile.getMyTalentCategories()),
                getCategoryResList(profile.getWantTalentCategories()),
                profile.isVisible()
        );
    }
    private static List<ProfileCategoryRes> getCategoryResList(List<Category> list){
        return list.stream().map(ProfileCategoryRes::new).toList();
    }
}
