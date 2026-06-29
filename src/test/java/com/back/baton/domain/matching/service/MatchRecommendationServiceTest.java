package com.back.baton.domain.matching.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.matching.repository.MatchRecommendationQueryRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.MatchingErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MatchRecommendationServiceTest {

    private static final String SENT_PENDING_PROPOSAL_REASON = "이미 보낸 교환 제안이 대기 중입니다.";
    private static final String RECEIVED_PENDING_PROPOSAL_REASON = "상대가 보낸 교환 제안이 있습니다. 받은 제안을 확인해 주세요.";
    private static final String TRADE_IN_PROGRESS_REASON = "이미 진행 중인 교환 거래가 있습니다.";

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private MatchRecommendationQueryRepository matchRecommendationQueryRepository;

    @Mock
    private MatchProposalRepository matchProposalRepository;

    @InjectMocks
    private MatchRecommendationService matchRecommendationService;

    @Test
    @DisplayName("요청자의 재능 기준으로 추천 상대 목록을 조회한다")
    void getMatchRecommendations() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long categoryId = 10L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        Category category = createCategory(categoryId);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        providerId,
                        categoryId,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                categoryId,
                requesterId
        )).willReturn(recommendations);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId);

        assertThat(result).hasSize(1);

        MatchRecommendationRes recommendation = result.get(0);

        assertThat(recommendation.talentId()).isEqualTo(providerTalentId);
        assertThat(recommendation.providerId()).isEqualTo(providerId);
        assertThat(recommendation.proposalRequestEnabled()).isTrue();
        assertThat(recommendation.proposalRequestDisabledReason()).isNull();

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendations(categoryId, requesterId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(requesterId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should()
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("이미 보낸 대기 중 제안이 있는 추천 상대는 제안 요청이 비활성화된다")
    void getMatchRecommendations_disabledBySentPendingProposal() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long categoryId = 10L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        Category category = createCategory(categoryId);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        providerId,
                        categoryId,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                categoryId,
                requesterId
        )).willReturn(recommendations);

        given(matchProposalRepository.existsSentPendingProposal(
                requesterId,
                requesterTalentId,
                providerTalentId
        )).willReturn(true);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId);

        assertThat(result).hasSize(1);

        MatchRecommendationRes recommendation = result.get(0);

        assertThat(recommendation.talentId()).isEqualTo(providerTalentId);
        assertThat(recommendation.proposalRequestEnabled()).isFalse();
        assertThat(recommendation.proposalRequestDisabledReason())
                .isEqualTo(SENT_PENDING_PROPOSAL_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendations(categoryId, requesterId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(requesterId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("상대가 보낸 대기 중 제안이 있는 추천 상대는 제안 요청이 비활성화된다")
    void getMatchRecommendations_disabledByReceivedPendingProposal() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long categoryId = 10L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        Category category = createCategory(categoryId);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        providerId,
                        categoryId,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                categoryId,
                requesterId
        )).willReturn(recommendations);

        given(matchProposalRepository.existsReceivedPendingProposal(
                requesterId,
                requesterTalentId,
                providerId,
                providerTalentId
        )).willReturn(true);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId);

        assertThat(result).hasSize(1);

        MatchRecommendationRes recommendation = result.get(0);

        assertThat(recommendation.talentId()).isEqualTo(providerTalentId);
        assertThat(recommendation.proposalRequestEnabled()).isFalse();
        assertThat(recommendation.proposalRequestDisabledReason())
                .isEqualTo(RECEIVED_PENDING_PROPOSAL_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendations(categoryId, requesterId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(requesterId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("진행 중인 거래가 있는 추천 상대는 제안 요청이 비활성화된다")
    void getMatchRecommendations_disabledByTradeInProgress() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long categoryId = 10L;
        Long providerId = 4L;
        Long providerTalentId = 3L;

        Category category = createCategory(categoryId);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        List<MatchRecommendationRes> recommendations = List.of(
                new MatchRecommendationRes(
                        providerTalentId,
                        providerId,
                        categoryId,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendations(
                categoryId,
                requesterId
        )).willReturn(recommendations);

        given(matchProposalRepository.existsTradeInProgressProposal(
                requesterId,
                requesterTalentId,
                providerTalentId
        )).willReturn(true);

        List<MatchRecommendationRes> result =
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId);

        assertThat(result).hasSize(1);

        MatchRecommendationRes recommendation = result.get(0);

        assertThat(recommendation.talentId()).isEqualTo(providerTalentId);
        assertThat(recommendation.proposalRequestEnabled()).isFalse();
        assertThat(recommendation.proposalRequestDisabledReason())
                .isEqualTo(TRADE_IN_PROGRESS_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendations(categoryId, requesterId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(requesterId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(requesterId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should()
                .existsTradeInProgressProposal(requesterId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("존재하지 않는 재능이면 예외가 발생한다")
    void getMatchRecommendations_talentNotFound() {
        Long requesterTalentId = 999L;
        Long requesterId = 2L;

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).shouldHaveNoInteractions();
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다른 사용자의 재능으로는 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_accessDenied() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;
        Long actualAuthorId = 999L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, actualAuthorId, category);

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).shouldHaveNoInteractions();
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("삭제된 재능이면 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_deletedTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);
        requesterTalent.softDelete();

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).shouldHaveNoInteractions();
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 재능이면 추천 상대를 조회할 수 없다")
    void getMatchRecommendations_inactiveTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        Category category = createCategory(10L);
        Talent requesterTalent = createTalent(requesterTalentId, requesterId, category);

        ReflectionTestUtils.setField(requesterTalent, "status", TalentStatus.CLOSED);

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() ->
                matchRecommendationService.getMatchRecommendations(requesterTalentId, requesterId)
        ).isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).shouldHaveNoInteractions();
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("매칭 추천 상대 상세 조회 성공")
    void getMatchRecommendationDetail_success() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long providerId = 3L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        MatchRecommendationDetailRes detail = new MatchRecommendationDetailRes(
                providerTalentId,
                providerId,
                1L,
                "디자인",
                "Figma 와이어프레임 제작",
                "초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다.",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "이미자",
                "Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다.",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.of(detail));

        MatchRecommendationDetailRes result =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        assertThat(result.talentId()).isEqualTo(providerTalentId);
        assertThat(result.providerId()).isEqualTo(providerId);
        assertThat(result.categoryName()).isEqualTo("디자인");
        assertThat(result.title()).isEqualTo("Figma 와이어프레임 제작");
        assertThat(result.proposalRequestEnabled()).isTrue();
        assertThat(result.proposalRequestDisabledReason()).isNull();

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(userId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(userId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should()
                .existsTradeInProgressProposal(userId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("자기 자신의 재능을 추천 상세 조회하면 예외가 발생한다")
    void getMatchRecommendationDetail_selfMatching_throwsException() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        MatchRecommendationDetailRes detail = new MatchRecommendationDetailRes(
                providerTalentId,
                userId,
                category.getId(),
                "백엔드",
                "Spring Boot 과외 가능합니다",
                "Spring Boot 알려드립니다",
                150,
                2,
                BigDecimal.valueOf(4.5),
                3,
                100,
                "user1",
                "소개1",
                null,
                BigDecimal.valueOf(100),
                true,
                null
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.of(detail));

        assertThatThrownBy(() -> matchRecommendationService.getMatchRecommendationDetail(
                requesterTalentId,
                providerTalentId,
                userId
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED.getMessage());

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 보낸 대기 중 제안이 있으면 추천 상대 상세 조회 시 제안 요청이 비활성화된다")
    void getMatchRecommendationDetail_withSentPendingProposal_disabled() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long providerId = 3L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        MatchRecommendationDetailRes detail = new MatchRecommendationDetailRes(
                providerTalentId,
                providerId,
                category.getId(),
                "디자인",
                "Figma 와이어프레임 제작",
                "초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다.",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "이미자",
                "Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다.",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.of(detail));

        given(matchProposalRepository.existsSentPendingProposal(
                userId,
                requesterTalentId,
                providerTalentId
        )).willReturn(true);

        MatchRecommendationDetailRes result =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        assertThat(result.talentId()).isEqualTo(providerTalentId);
        assertThat(result.proposalRequestEnabled()).isFalse();
        assertThat(result.proposalRequestDisabledReason())
                .isEqualTo(SENT_PENDING_PROPOSAL_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(userId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsReceivedPendingProposal(userId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsTradeInProgressProposal(userId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("상대가 보낸 대기 중 제안이 있으면 추천 상대 상세 조회 시 제안 요청이 비활성화된다")
    void getMatchRecommendationDetail_withReceivedPendingProposal_disabled() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long providerId = 3L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        MatchRecommendationDetailRes detail = new MatchRecommendationDetailRes(
                providerTalentId,
                providerId,
                category.getId(),
                "디자인",
                "Figma 와이어프레임 제작",
                "초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다.",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "이미자",
                "Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다.",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.of(detail));

        given(matchProposalRepository.existsReceivedPendingProposal(
                userId,
                requesterTalentId,
                providerId,
                providerTalentId
        )).willReturn(true);

        MatchRecommendationDetailRes result =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        assertThat(result.talentId()).isEqualTo(providerTalentId);
        assertThat(result.proposalRequestEnabled()).isFalse();
        assertThat(result.proposalRequestDisabledReason())
                .isEqualTo(RECEIVED_PENDING_PROPOSAL_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(userId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(userId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should(never())
                .existsTradeInProgressProposal(userId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("진행 중인 거래가 있으면 추천 상대 상세 조회 시 제안 요청이 비활성화된다")
    void getMatchRecommendationDetail_withTradeInProgress_disabled() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long providerId = 3L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        MatchRecommendationDetailRes detail = new MatchRecommendationDetailRes(
                providerTalentId,
                providerId,
                category.getId(),
                "디자인",
                "Figma 와이어프레임 제작",
                "초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다.",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "이미자",
                "Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다.",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.of(detail));

        given(matchProposalRepository.existsTradeInProgressProposal(
                userId,
                requesterTalentId,
                providerTalentId
        )).willReturn(true);

        MatchRecommendationDetailRes result =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        assertThat(result.talentId()).isEqualTo(providerTalentId);
        assertThat(result.proposalRequestEnabled()).isFalse();
        assertThat(result.proposalRequestDisabledReason())
                .isEqualTo(TRADE_IN_PROGRESS_REASON);

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).should()
                .existsSentPendingProposal(userId, requesterTalentId, providerTalentId);
        then(matchProposalRepository).should()
                .existsReceivedPendingProposal(userId, requesterTalentId, providerId, providerTalentId);
        then(matchProposalRepository).should()
                .existsTradeInProgressProposal(userId, requesterTalentId, providerTalentId);
    }

    @Test
    @DisplayName("추천 상대 상세 정보가 없으면 예외가 발생한다")
    void getMatchRecommendationDetail_notFound_throwsException() {
        Long requesterTalentId = 1L;
        Long providerTalentId = 999L;
        Long userId = 1L;

        Category category = createCategory(1L);
        Talent requesterTalent = createTalent(requesterTalentId, userId, category);

        given(talentRepository.findById(requesterTalentId))
                .willReturn(Optional.of(requesterTalent));

        given(matchRecommendationQueryRepository.findMatchRecommendationDetail(
                category.getId(),
                providerTalentId
        )).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchRecommendationService.getMatchRecommendationDetail(
                requesterTalentId,
                providerTalentId,
                userId
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(TalentErrorCode.TALENT_NOT_FOUND.getMessage());

        then(talentRepository).should().findById(requesterTalentId);
        then(matchRecommendationQueryRepository).should()
                .findMatchRecommendationDetail(category.getId(), providerTalentId);
        then(matchProposalRepository).shouldHaveNoInteractions();
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