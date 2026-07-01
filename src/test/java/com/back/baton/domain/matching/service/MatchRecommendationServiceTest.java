package com.back.baton.domain.matching.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.matching.repository.MatchRecommendationQueryRepository;
import com.back.baton.domain.profile.repository.ProfileRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.MatchingErrorCode;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MatchRecommendationServiceTest {

    private static final String SENT_PENDING_PROPOSAL_REASON = "이미 보낸 교환 제안이 대기 중입니다.";

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private MatchRecommendationQueryRepository matchRecommendationQueryRepository;

    @Mock
    private MatchProposalRepository matchProposalRepository;

    @InjectMocks
    private MatchRecommendationService matchRecommendationService;

    @BeforeEach
    void setUp() {
        lenient().when(profileRepository.findWantTalentCategoriesByUserId(anyLong()))
                .thenReturn(List.of(createCategory(999L)));
    }

    @Test
    @DisplayName("requester want categories are required before recommendations")
    void getMatchRecommendations_withoutWantTalentCategory_throwsException() {
        Long requesterId = 2L;

        given(profileRepository.findWantTalentCategoriesByUserId(requesterId))
                .willReturn(List.of());

        assertThatThrownBy(() -> matchRecommendationService.getMatchRecommendations(requesterId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MatchingErrorCode.WANT_TALENT_CATEGORY_REQUIRED.getMessage());

        then(matchRecommendationQueryRepository).shouldHaveNoInteractions();
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("recommendations use requester registered talents and profile want categories")
    void getMatchRecommendations() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        requesterTalentId,
                        providerId,
                        999L,
                        "Design",
                        "Figma lesson",
                        "Figma wireframe lesson",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                anyList(),
                eq(requesterId)
        )).willReturn(recommendations);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).talentId()).isEqualTo(providerTalentId);
        assertThat(result.get(0).requesterTalentId()).isEqualTo(requesterTalentId);
        assertThat(result.get(0).proposalRequestEnabled()).isTrue();

        then(profileRepository).should().findWantTalentCategoriesByUserId(requesterId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendations(anyList(), eq(requesterId));
        then(matchProposalRepository).should()
                .existsSentPendingProposal(requesterId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should()
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("sent pending proposal disables recommendation request")
    void getMatchRecommendations_disabledBySentPendingProposal() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        requesterTalentId,
                        providerId,
                        999L,
                        "Design",
                        "Figma lesson",
                        "Figma wireframe lesson",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                anyList(),
                eq(requesterId)
        )).willReturn(recommendations);
        given(matchProposalRepository.existsSentPendingProposal(
                requesterId,
                requesterTalentId,
                providerTalentId
        )).willReturn(true);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterId);

        assertThat(result.get(0).proposalRequestEnabled()).isFalse();
        assertThat(result.get(0).proposalRequestDisabledReason())
                .isEqualTo(SENT_PENDING_PROPOSAL_REASON);
        then(matchProposalRepository).should(never())
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("recommendation detail uses selected requester talent")
    void getMatchRecommendationDetail_success() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long providerId = 3L;
        Long userId = 1L;
        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);
        MatchRecommendationDetailRes detail = createDetail(providerTalentId, providerId, 999L);

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));
        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                eq(category.getId()),
                anyList(),
                eq(providerTalentId)
        )).willReturn(Optional.of(detail));

        MatchRecommendationDetailRes result =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        assertThat(result.talentId()).isEqualTo(providerTalentId);
        assertThat(result.proposalRequestEnabled()).isTrue();
    }

    @Test
    @DisplayName("recommendation detail rejects self matching")
    void getMatchRecommendationDetail_selfMatching_throwsException() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long userId = 1L;
        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);
        MatchRecommendationDetailRes detail = createDetail(providerTalentId, userId, 999L);

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));
        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                eq(category.getId()),
                anyList(),
                eq(providerTalentId)
        )).willReturn(Optional.of(detail));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                )
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED.getMessage());

        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    private MatchRecommendationDetailRes createDetail(Long talentId, Long providerId, Long categoryId) {
        return new MatchRecommendationDetailRes(
                talentId,
                providerId,
                categoryId,
                "Design",
                "Figma lesson",
                "Figma wireframe lesson",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "provider",
                "provider introduction",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );
    }

    private Talent createTalent(Long id, Long authorId, Category category) {
        Talent talent = Talent.create(
                authorId,
                category,
                "test talent",
                "test content",
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
            ReflectionTestUtils.setField(category, "name", "Backend");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);

            return category;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test category", e);
        }
    }
}
