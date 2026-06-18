package com.back.baton.domain.matching.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.matching.repository.MatchRecommendationQueryRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchRecommendationServiceTest {

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private MatchProposalRepository matchProposalRepository;

    @Mock
    private MatchRecommendationQueryRepository matchRecommendationQueryRepository;

    @InjectMocks
    private MatchRecommendationService matchRecommendationService;

    @Test
    @DisplayName("요청자의 재능 기준으로 추천 상대 목록을 조회한다")
    void getMatchRecommendations() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long categoryId = 10L;

        Category category = createCategory(categoryId);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        List<Long> excludedTalentIds = List.of(2L);

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        3L,
                        4L,
                        categoryId,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1
                )
        );

        when(talentRepository.findById(requesterTalentId))
                .thenReturn(Optional.of(requesterTalent));

        when(matchProposalRepository.findRequestedProviderTalentIds(
                requesterId,
                requesterTalentId,
                MatchProposalStatus.REQUESTED
        )).thenReturn(excludedTalentIds);

        when(matchRecommendationQueryRepository.findMatchRecommendations(
                categoryId,
                requesterId,
                excludedTalentIds
        )).thenReturn(recommendations);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).talentId()).isEqualTo(3L);
        assertThat(result.get(0).providerId()).isEqualTo(4L);

        verify(talentRepository).findById(requesterTalentId);
        verify(matchProposalRepository).findRequestedProviderTalentIds(
                requesterId,
                requesterTalentId,
                MatchProposalStatus.REQUESTED
        );
        verify(matchRecommendationQueryRepository).findMatchRecommendations(
                categoryId,
                requesterId,
                excludedTalentIds
        );
    }

    @Test
    @DisplayName("존재하지 않는 재능이면 예외가 발생한다")
    void getMatchRecommendations_talentNotFound() {
        Long requesterTalentId = 999L;
        Long requesterId = 2L;

        when(talentRepository.findById(requesterTalentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        verify(talentRepository).findById(requesterTalentId);
    }

    @Test
    @DisplayName("다른 사용자의 재능으로는 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_accessDenied() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long actualAuthorId = 999L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, actualAuthorId, category);

        when(talentRepository.findById(requesterTalentId))
                .thenReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        verify(talentRepository).findById(requesterTalentId);
    }

    @Test
    @DisplayName("삭제된 재능이면 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_deletedTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);
        requesterTalent.softDelete();

        when(talentRepository.findById(requesterTalentId))
                .thenReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        verify(talentRepository).findById(requesterTalentId);
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 재능이면 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_inactiveTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        ReflectionTestUtils.setField(requesterTalent, "status", TalentStatus.CLOSED);

        when(talentRepository.findById(requesterTalentId))
                .thenReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        verify(talentRepository).findById(requesterTalentId);
    }

    private Talent createTalent(Long id, Long authorId, Category category) {
        Talent talent = Talent.create(
                authorId,
                category,
                "테스트 재능",
                "테스트 내용",
                2,
                100
        );

        ReflectionTestUtils.setField(talent, "id", id);

        return talent;
    }

    private Category createCategory(Long id) {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Category category = constructor.newInstance();

            ReflectionTestUtils.setField(category, "id", id);
            ReflectionTestUtils.setField(category, "name", "백엔드");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);

            return category;
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }
}