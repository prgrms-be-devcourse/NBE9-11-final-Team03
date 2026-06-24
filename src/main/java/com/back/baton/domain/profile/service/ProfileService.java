package com.back.baton.domain.profile.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.profile.dto.request.ProfileUpdateReq;
import com.back.baton.domain.profile.dto.response.MyProfileDetailRes;
import com.back.baton.domain.profile.dto.response.ProfileUpdateRes;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.profile.repository.ProfileRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ProfileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;

    public void initializeProfile(User user){ // User 생성시 profile 생성
        Profile profile = new Profile(user);
        profileRepository.save(profile);
    }

    public ProfileUpdateRes updateProfile(Long userId, ProfileUpdateReq req) {
        // 프로필이 없다면 에러 반환
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // 가진/원하는 재능 카테고리 ID -> 카테고리 매핑 및 검증
        List<Category> myTalentCategories = getTalentCategoriesById(req.myTalentCategoryIds());
        List<Category> wantTalentCategories = getTalentCategoriesById(req.wantTalentCategoryIds());

        // 프로필 업데이트 (재능 카테고리, 교환 원하는 재능 카테고리, 포트폴리오 링크)
        profile.update(myTalentCategories, wantTalentCategories, req.portfolioLinkList());

        // 연관된 유저 업데이트
        String introduction = req.introduction();
        if(introduction!=null){
            introduction = introduction.strip();
        }
        if(introduction!=null&& introduction.length()<5){
            throw new CustomException(ProfileErrorCode.INVALID_INTRODUCTION);
        }
        profile.getUser().updateProfile(req.profileImageUrl(), introduction);

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

    @Transactional(readOnly = true)
    public MyProfileDetailRes getMyProfile(Long userId){
        Profile profile = profileRepository.findDetailByUserId(userId)
                .orElseThrow(()->new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));

        return new MyProfileDetailRes(profile);
    }
}
