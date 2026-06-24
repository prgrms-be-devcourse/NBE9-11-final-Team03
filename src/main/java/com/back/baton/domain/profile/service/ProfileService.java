package com.back.baton.domain.profile.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.profile.dto.response.ProfileUpdateRes;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.profile.repository.ProfileRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ProfileErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public void initializeProfile(User user){ // User 생성시 profile 생성
        Profile profile = new Profile(user);
        profileRepository.save(profile);
    }

    public ProfileUpdateRes updateProfile(Long userId, String profileImageUrl, String introduction,
                                          List<Long> myTalentCategoryIds, List<Long> wantTalentCategoryIds, List<String> portfolioLinkList) {
        // 프로필이 없다면 에러 반환
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // 가진/원하는 재능 카테고리 ID -> 카테고리 매핑 및 검증
        List<Category> myTalentCategories = getTalentCategoriesById(myTalentCategoryIds);
        List<Category> wantTalentCategories = getTalentCategoriesById(wantTalentCategoryIds);

        // 프로필 업데이트 (재능 카테고리, 교환 원하는 재능 카테고리, 포트폴리오 링크)
        profile.update(myTalentCategories, wantTalentCategories, portfolioLinkList);

        // 연관된 유저 업데이트
        if(introduction!=null&& introduction.length()<5){
            throw new CustomException(ProfileErrorCode.INVALID_INTRODUCTION);
        }
        profile.getUser().updateProfile(profileImageUrl,introduction);

        profileRepository.save(profile);
        return new ProfileUpdateRes(profile);
    }

    private List<Category> getTalentCategoriesById(List<Long> idList){
        // 재능 카테고리 ID 리스트 -> 재능 카테고리 리스트
        // active 상태가 아닌 게 있거나 카테고리가 없으면 에러 반환
        List<Category> categories = idList == null? null
                : categoryRepository.findAllById(idList).stream()
                    .filter(Category::isActive).toList();

        if(idList!=null && categories.size()!=idList.size()){
            throw new CustomException(ProfileErrorCode.INVALID_CATEGORIES);
        }
        return categories;
    }
}
