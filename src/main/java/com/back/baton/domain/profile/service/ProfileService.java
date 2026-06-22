package com.back.baton.domain.profile.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.profile.dto.response.ProfileUpdateRes;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.profile.repository.ProfileRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
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
        // 프로필이 없다면 유저 정보로 생성
        Profile profile = getOrCreateProfile(userId);

        // 가진/원하는 재능 카테고리 ID -> 카테고리 매핑 및 Active만 필터링
        List<Category> myTalentCategories = getTalentCategoriesById(myTalentCategoryIds);
        List<Category> wantTalentCategories = getTalentCategoriesById(wantTalentCategoryIds);

        // 프로필 업데이트 (재능 카테고리, 교환 원하는 재능 카테고리, 포트폴리오 링크)
        profile.update(myTalentCategories, wantTalentCategories, portfolioLinkList);

        // 연관된 유저 업데이트
        introduction = introduction!=null&& introduction.length()<5? null : introduction;
        profile.getUser().updateProfile(profileImageUrl,introduction);

        profileRepository.save(profile);
        return new ProfileUpdateRes(profile);
    }

    private Profile getOrCreateProfile(Long userId){
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return profileRepository.save(new Profile(user));
                });
    }
    private List<Category> getTalentCategoriesById(List<Long> idList){
        // 재능 카테고리 ID 리스트 -> 재능 카테고리 리스트
        // active 상태인 것만 반영
        return idList == null? null
                : categoryRepository.findAllById(idList).stream()
                    .filter(Category::isActive).toList();
    }
}
