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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {
    @InjectMocks
    private ProfileService profileService;

    @Mock private ProfileRepository profileRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder().build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testProfile = new Profile(testUser);
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 모든 데이터 정상일 때")
    void updateProfile_Success() {
        // given
        given(profileRepository.findByUserId(1L)).willReturn(Optional.of(testProfile));

        List<Long> categoryIds = List.of(1L);
        Category activeCategory = createTestCategory(1L, true);

        given(categoryRepository.findAllById(categoryIds)).willReturn(List.of(activeCategory));

        // when
        ProfileUpdateRes response = profileService.updateProfile(1L, "url", "소개글은 5자 이상", categoryIds, null, null);

        // then
        assertThat(response).isNotNull();
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 소개글이 5자 미만이면 null 처리")
    void updateProfile_ShortIntroduction_BecomesNull() {
        // given
        given(profileRepository.findByUserId(1L)).willReturn(Optional.of(testProfile));

        // when
        profileService.updateProfile(1L, "url", "짧음", null, null, null);

        // then
        assertThat(testUser.getIntroduction()).isNull();
    }

    @Test
    @DisplayName("카테고리 ID 리스트가 들어올 때 Active 상태인 것만 필터링한다")
    void getTalentCategoriesById_FilterActiveOnly() {
        // given
        Category active = createTestCategory(1L, true);
        Category inactive = createTestCategory(2L, false);

        given(profileRepository.findByUserId(1L)).willReturn(Optional.of(testProfile));
        given(categoryRepository.findAllById(anyList())).willReturn(List.of(active, inactive));

        // when
        profileService.updateProfile(1L, "url", "소개글은 5자 이상", List.of(1L, 2L), null, null);

        // then
        assertThat(testProfile.getMyTalentCategories()).hasSize(1);
        assertThat(testProfile.getMyTalentCategories().get(0)).isEqualTo(active);
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외 발생")
    void updateProfile_UserNotFound() {
        // given
        given(profileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.updateProfile(1L, null, null, null, null, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    // 테스트용 Category 생성 헬퍼 메서드
    private Category createTestCategory(Long id, boolean active) {
        Category category = Category.create("name", 1);
        ReflectionTestUtils.setField(category, "id", id);
        ReflectionTestUtils.setField(category, "active", active);
        return category;
    }
}