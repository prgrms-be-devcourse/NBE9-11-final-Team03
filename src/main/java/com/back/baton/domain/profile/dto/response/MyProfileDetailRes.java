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
    public static MyProfileDetailRes of(
            Profile profile,
            List<String> portfolioLinkList,
            List<Category> myTalentCategories,
            List<Category> wantTalentCategories
    ) {
        return new MyProfileDetailRes(
                profile.getId(),
                profile.getUser().getNickname(),
                profile.getUser().getProfileImageUrl(),
                profile.getUser().getIntroduction(),
                profile.getUser().getTrustScore(),
                portfolioLinkList,
                getCategoryResList(myTalentCategories),
                getCategoryResList(wantTalentCategories),
                profile.isVisible()
        );
    }

    private static List<ProfileCategoryRes> getCategoryResList(List<Category> list){
        return list.stream().map(ProfileCategoryRes::new).toList();
    }
}
