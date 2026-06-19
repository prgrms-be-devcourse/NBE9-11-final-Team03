package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
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

    @Test
    @DisplayName("같은 카테고리의 ACTIVE 재능 중 본인 재능과 삭제된 재능을 제외하고 추천 목록을 조회한다")
    void findMatchRecommendations() {
        Long requesterId = 2L;

        Category backend = saveCategory("백엔드", 1);
        Category frontend = saveCategory("프론트엔드", 2);

        saveTalent(requesterId, backend, "요청자 본인 재능", 0, 0, BigDecimal.ZERO);
        Talent recommended1 = saveTalent(3L, backend, "Spring Boot 과외 가능합니다", 10, 3, BigDecimal.valueOf(4.50));
        Talent recommended2 = saveTalent(4L, backend, "MySQL 튜닝 도와드립니다", 5, 1, BigDecimal.valueOf(4.00));

        saveTalent(requesterId, backend, "요청자 본인 다른 재능", 100, 100, BigDecimal.valueOf(5.00));
        saveTalent(5L, frontend, "다른 카테고리 재능", 100, 100, BigDecimal.valueOf(5.00));

        Talent deletedTalent = saveTalent(6L, backend, "삭제된 재능", 100, 100, BigDecimal.valueOf(5.00));
        deletedTalent.softDelete();
        talentRepository.save(deletedTalent);

        List<MatchRecommendationRes> result = matchRecommendationQueryRepository.findMatchRecommendations(
                backend.getId(),
                requesterId
        );

        assertThat(result).extracting(MatchRecommendationRes::talentId)
                .containsExactly(recommended1.getId(), recommended2.getId());

        assertThat(result).extracting(MatchRecommendationRes::providerId)
                .containsExactly(3L, 4L);
    }

    @Test
    @DisplayName("추천 목록은 평점, 완료 수, 조회 수, id 순으로 정렬된다")
    void findMatchRecommendations_orderByRecommendationScore() {
        Long requesterId = 2L;

        Category backend = saveCategory("백엔드", 1);

        saveTalent(requesterId, backend, "요청자 본인 재능", 0, 0, BigDecimal.ZERO);

        Talent lowRating = saveTalent(3L, backend, "평점 낮은 재능", 100, 100, BigDecimal.valueOf(3.00));
        Talent highRatingLowComplete = saveTalent(4L, backend, "평점 높은 재능 1", 10, 1, BigDecimal.valueOf(4.50));
        Talent highRatingHighComplete = saveTalent(5L, backend, "평점 높은 재능 2", 5, 3, BigDecimal.valueOf(4.50));

        List<MatchRecommendationRes> result = matchRecommendationQueryRepository.findMatchRecommendations(
                backend.getId(),
                requesterId
        );

        assertThat(result).extracting(MatchRecommendationRes::talentId)
                .containsExactly(
                        highRatingHighComplete.getId(),
                        highRatingLowComplete.getId(),
                        lowRating.getId()
                );
    }

    private Category saveCategory(String name, int sortOrder) {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Category category = constructor.newInstance();

            ReflectionTestUtils.setField(category, "name", name);
            ReflectionTestUtils.setField(category, "sortOrder", sortOrder);
            ReflectionTestUtils.setField(category, "active", true);

            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
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
                "테스트 내용",
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