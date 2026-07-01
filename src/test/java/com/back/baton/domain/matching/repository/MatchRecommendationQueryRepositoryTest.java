package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.profile.repository.ProfileRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class, MatchRecommendationQueryRepository.class})
class MatchRecommendationQueryRepositoryTest {

    @Autowired
    private MatchRecommendationQueryRepository matchRecommendationQueryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("상대가 내 재능 카테고리를 원하고 상대 재능이 내 희망 카테고리에 속하면 추천한다")
    void findMatchRecommendations() {
        Category backend = categoryRepository.save(Category.create("Backend", 1));
        Category frontend = categoryRepository.save(Category.create("Frontend", 2));
        Category design = categoryRepository.save(Category.create("Design", 3));

        User requester = saveUser("requester@test.com", "requester");
        User provider1 = saveUser("provider1@test.com", "provider1");
        User provider2 = saveUser("provider2@test.com", "provider2");
        User unmatchedProvider = saveUser("unmatched@test.com", "unmatched");
        User deletedProvider = saveUser("deleted@test.com", "deleted");

        saveProfile(provider1, List.of(backend));
        saveProfile(provider2, List.of(backend));
        saveProfile(unmatchedProvider, List.of(backend));
        saveProfile(deletedProvider, List.of(backend));

        Talent requesterTalent = saveTalent(requester.getId(), backend, "requester backend", 0, 0, BigDecimal.ZERO);
        saveTalent(requester.getId(), backend, "requester backend duplicate category", 0, 0, BigDecimal.ZERO);
        Talent recommended1 = saveTalent(provider1.getId(), design, "figma lesson", 10, 3, BigDecimal.valueOf(4.50));
        Talent recommended2 = saveTalent(provider2.getId(), design, "ux review", 5, 1, BigDecimal.valueOf(4.00));

        saveTalent(requester.getId(), design, "requester design", 100, 100, BigDecimal.valueOf(5.00));
        saveTalent(unmatchedProvider.getId(), frontend, "not wanted category", 100, 100, BigDecimal.valueOf(5.00));

        Talent deletedTalent = saveTalent(deletedProvider.getId(), design, "deleted talent", 100, 100, BigDecimal.valueOf(5.00));
        deletedTalent.softDelete();
        talentRepository.save(deletedTalent);

        List<MatchRecommendationRes> result = matchRecommendationQueryRepository.findMatchRecommendations(
                List.of(design.getId()),
                requester.getId()
        );

        assertThat(result).extracting(MatchRecommendationRes::talentId)
                .containsExactly(recommended1.getId(), recommended2.getId());

        assertThat(result).extracting(MatchRecommendationRes::providerId)
                .containsExactly(provider1.getId(), provider2.getId());

        assertThat(result).extracting(MatchRecommendationRes::requesterTalentId)
                .containsExactly(requesterTalent.getId(), requesterTalent.getId());
    }

    @Test
    @DisplayName("추천 목록은 평점, 완료 수, 조회 수, id 순서로 정렬한다")
    void findMatchRecommendations_orderByRecommendationScore() {
        Category backend = categoryRepository.save(Category.create("Backend", 1));
        Category design = categoryRepository.save(Category.create("Design", 2));

        User requester = saveUser("requester-order@test.com", "requesterOrder");
        User lowRatingProvider = saveUser("low-rating@test.com", "lowRating");
        User highRatingLowCompleteProvider = saveUser("high-low@test.com", "highLow");
        User highRatingHighCompleteProvider = saveUser("high-high@test.com", "highHigh");

        saveProfile(lowRatingProvider, List.of(backend));
        saveProfile(highRatingLowCompleteProvider, List.of(backend));
        saveProfile(highRatingHighCompleteProvider, List.of(backend));

        saveTalent(requester.getId(), backend, "requester backend", 0, 0, BigDecimal.ZERO);

        Talent lowRating = saveTalent(lowRatingProvider.getId(), design, "low rating", 100, 100, BigDecimal.valueOf(3.00));
        Talent highRatingLowComplete = saveTalent(highRatingLowCompleteProvider.getId(), design, "high rating low complete", 10, 1, BigDecimal.valueOf(4.50));
        Talent highRatingHighComplete = saveTalent(highRatingHighCompleteProvider.getId(), design, "high rating high complete", 5, 3, BigDecimal.valueOf(4.50));

        List<MatchRecommendationRes> result = matchRecommendationQueryRepository.findMatchRecommendations(
                List.of(design.getId()),
                requester.getId()
        );

        assertThat(result).extracting(MatchRecommendationRes::talentId)
                .containsExactly(
                        highRatingHighComplete.getId(),
                        highRatingLowComplete.getId(),
                        lowRating.getId()
                );
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password("password")
                        .nickname(nickname)
                        .profileImageUrl(null)
                        .introduction("test introduction")
                        .trustScore(BigDecimal.ZERO)
                        .build()
        );
    }

    private Profile saveProfile(User user, List<Category> wantTalentCategories) {
        Profile profile = new Profile(user);
        profile.update(null, wantTalentCategories, null);
        return profileRepository.save(profile);
    }

    private Talent saveTalent(
            Long authorId,
            Category category,
            String title,
            int viewCount,
            int completeCount,
            BigDecimal avgRating
    ) {
        Talent talent = Talent.create(
                authorId,
                category,
                title,
                "test content",
                2,
                100
        );

        ReflectionTestUtils.setField(talent, "viewCount", viewCount);
        ReflectionTestUtils.setField(talent, "completeCount", completeCount);
        ReflectionTestUtils.setField(talent, "avgRating", avgRating);
        ReflectionTestUtils.setField(talent, "status", TalentStatus.ACTIVE);

        return talentRepository.save(talent);
    }
}
